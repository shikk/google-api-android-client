/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.concurrent.GuardedBy;

/**
 * Base class for services that can implement {@link #startUp} and {@link #shutDown} but while in
 * the "running" state need to perform a periodic task.  Subclasses can implement {@link #startUp},
 * {@link #shutDown} and also a {@link #runOneIteration} method that will be executed periodically.
 *
 * <p>This class uses the {@link ScheduledExecutorService} returned from {@link #executor} to run
 * the {@link #startUp} and {@link #shutDown} methods and also uses that service to schedule the
 * {@link #runOneIteration} that will be executed periodically as specified by its
 * {@link Scheduler}. When this service is asked to stop via {@link #stop} or {@link #stopAndWait},
 * it will cancel the periodic task (but not interrupt it) and wait for it to stop before running
 * the {@link #shutDown} method.
 *
 * <p>Subclasses are guaranteed that the life cycle methods ({@link #runOneIteration}, {@link
 * #startUp} and {@link #shutDown}) will never run concurrently. Notably, if any execution of {@link
 * #runOneIteration} takes longer than its schedule defines, then subsequent executions may start
 * late.  Also, all life cycle methods are executed with a lock held, so subclasses can safely
 * modify shared state without additional synchronization necessary for visibility to later
 * executions of the life cycle methods.
 *
 * <h3>Usage Example</h3>
 *
 * Here is a sketch of a service which crawls a website and uses the scheduling capabilities to
 * rate limit itself. <pre> {@code
 * class CrawlingService extends AbstractScheduledService {
 *   private Set<Uri> visited;
 *   private Queue<Uri> toCrawl;
 *   protected void startUp() throws Exception {
 *     toCrawl = readStartingUris();
 *   }
 *
 *   protected void runOneIteration() throws Exception {
 *     Uri uri = toCrawl.remove();
 *     Collection<Uri> newUris = crawl(uri);
 *     visited.add(uri);
 *     for (Uri newUri : newUris) {
 *       if (!visited.contains(newUri)) { toCrawl.add(newUri); }
 *     }
 *   }
 *
 *   protected void shutDown() throws Exception {
 *     saveUris(toCrawl);
 *   }
 *
 *   protected Scheduler scheduler() {
 *     return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.SECONDS);
 *   }
 * }}</pre>
 *
 * This class uses the life cycle methods to read in a list of starting URIs and save the set of
 * outstanding URIs when shutting down.  Also, it takes advantage of the scheduling functionality to
 * rate limit the number of queries we perform.
 *
 * @author Luke Sandberg
 * @since 11.0
 */
@Beta
public abstract class AbstractScheduledService implements Service {
  private static final Logger logger = Logger.getLogger(AbstractScheduledService.class.getName());

  /**
   * A scheduler defines the policy for how the {@link AbstractScheduledService} should run its
   * task.
   *
   * <p>Consider using the {@link #newFixedDelaySchedule} and {@link #newFixedRateSchedule} factory
   * methods, these provide {@link Scheduler} instances for the common use case of running the
   * service with a fixed schedule.  If more flexibility is needed then consider subclassing
   * {@link CustomScheduler}.
   *
   * @author Luke Sandberg
   * @since 11.0
   */
  public abstract static class Scheduler {
    /**
     * Returns a {@link Scheduler} that schedules the task using the
     * {@link ScheduledExecutorService#scheduleWithFixedDelay} method.
     *
     * @param initialDelay the time to delay first execution
     * @param delay the delay between the termination of one execution and the commencement of the
     *        next
     * @param unit the time unit of the initialDelay and delay parameters
     */
    public static Scheduler newFixedDelaySchedule(final long initialDelay, final long delay,
        final TimeUnit unit) {
      return new Scheduler() {
        @Override
        public Future<?> schedule(AbstractService service, ScheduledExecutorService executor,
            Runnable task) {
          return executor.scheduleWithFixedDelay(task, initialDelay, delay, unit);
        }
      };
    }

    /**
     * Returns a {@link Scheduler} that schedules the task using the
     * {@link ScheduledExecutorService#scheduleAtFixedRate} method.
     *
     * @param initialDelay the time to delay first execution
     * @param period the period between successive executions of the task
     * @param unit the time unit of the initialDelay and period parameters
     */
    public static Scheduler newFixedRateSchedule(final long initialDelay, final long period,
        final TimeUnit unit) {
      return new Scheduler() {
        @Override
        public Future<?> schedule(AbstractService service, ScheduledExecutorService executor,
            Runnable task) {
          return executor.scheduleAtFixedRate(task, initialDelay, period, unit);
        }
      };
    }

    /** Schedules the task to run on the provided executor on behalf of the service.  */
    abstract Future<?> schedule(AbstractService service, ScheduledExecutorService executor,
        Runnable runnable);

    private Scheduler() {}
  }

  /* use AbstractService for state management */
  private final AbstractService delegate = new AbstractService() {

    // A handle to the running task so that we can stop it when a shutdown has been requested.
    // These two fields are volatile because their values will be accessed from multiple threads.
    private volatile Future<?> runningTask;
    private volatile ScheduledExecutorService executorService;

    // This lock protects the task so we can ensure that none of the template methods (startUp,
    // shutDown or runOneIteration) run concurrently with one another.
    private final ReentrantLock lock = new ReentrantLock();

    private final Runnable task = new Runnable() {
      @Override public void run() {
        lock.lock();
        try {
          AbstractScheduledService.this.runOneIteration();
        } catch (Throwable t) {
          try {
            shutDown();
          } catch (Exception ignored) {
            logger.log(Level.WARNING,
                "Error while attempting to shut down the service after failure.", ignored);
          }
          notifyFailed(t);
          throw Throwables.propagate(t);
        } finally {
          lock.unlock();
        }
      }
    };

    @Override protected final void doStart() {
      executorService = executor();
      executorService.execute(new Runnable() {
        @Override public void run() {
          lock.lock();
          try {
            startUp();
            runningTask = scheduler().schedule(delegate, executorService, task);
            notifyStarted();
          } catch (Throwable t) {
            notifyFailed(t);
            throw Throwables.propagate(t);
          } finally {
            lock.unlock();
          }
        }
      });
    }

    @Override protected final void doStop() {
      runningTask.cancel(false);
      executorService.execute(new Runnable() {
        @Override public void run() {
          try {
            lock.lock();
            try {
              if (state() != State.STOPPING) {
                // This means that the state has changed since we were scheduled.  This implies that
                // an execution of runOneIteration has thrown an exception and we have transitioned
                // to a failed state, also this means that shutDown has already been called, so we
                // do not want to call it again.
                return;
              }
              shutDown();
            } finally {
              lock.unlock();
            }
            notifyStopped();
          } catch (Throwable t) {
            notifyFailed(t);
            throw Throwables.propagate(t);
          }
        }
      });
    }
  };

  /**
   * Run one iteration of the scheduled task. If any invocation of this method throws an exception,
   * the service will transition to the {@link Service.State#FAILED} state and this method will no
   * longer be called.
   */
  protected abstract void runOneIteration() throws Exception;

  /**
   * Start the service.
   *
   * <p>By default this method does nothing.
   */
  protected void startUp() throws Exception {}

  /**
   * Stop the service. This is guaranteed not to run concurrently with {@link #runOneIteration}.
   *
   * <p>By default this method does nothing.
   */
  protected void shutDown() throws Exception {}

  /**
   * Returns the {@link Scheduler} object used to configure this service.  This method will only be
   * called once.
   */
  protected abstract Scheduler scheduler();

  /**
   * Returns the {@link ScheduledExecutorService} that will be used to execute the {@link #startUp},
   * {@link #runOneIteration} and {@link #shutDown} methods.  The executor will not be
   * {@link ScheduledExecutorService#shutdown} when this service stops. Subclasses may override this
   * method to use a custom {@link ScheduledExecutorService} instance.
   *
   * <p>By default this returns a new {@link ScheduledExecutorService} with a single thread thread
   * pool.  This method will only be called once.
   */
  protected ScheduledExecutorService executor() {
    return Executors.newSingleThreadScheduledExecutor();
  }

  @Override public String toString() {
    return getClass().getSimpleName() + " [" + state() + "]";
  }

  // We override instead of using ForwardingService so that these can be final.

  @Override public final ListenableFuture<State> start() {
    return delegate.start();
  }

  @Override public final State startAndWait() {
    return delegate.startAndWait();
  }

  @Override public final boolean isRunning() {
    return delegate.isRunning();
  }

  @Override public final State state() {
    return delegate.state();
  }

  @Override public final ListenableFuture<State> stop() {
    return delegate.stop();
  }

  @Override public final State stopAndWait() {
    return delegate.stopAndWait();
  }

  @Override public final void addListener(Listener listener, Executor executor) {
    delegate.addListener(listener, executor);
  }

  /**
   * A {@link Scheduler} that provides a convenient way for the {@link AbstractScheduledService} to
   * use a dynamically changing schedule.  After every execution of the task, assuming it hasn't
   * been cancelled, the {@link #getNextSchedule} method will be called.
   *
   * @author Luke Sandberg
   * @since 11.0
   */
  @Beta
  public abstract static class CustomScheduler extends Scheduler {

    /**
     * A callable class that can reschedule itself using a {@link CustomScheduler}.
     */
    private class ReschedulableCallable extends ForwardingFuture<Void> implements Callable<Void> {

      /** The underlying task. */
      private final Runnable wrappedRunnable;

      /** The executor on which this Callable will be scheduled. */
      private final ScheduledExecutorService executor;

      /**
       * The service that is managing this callable.  This is used so that failure can be
       * reported properly.
       */
      private final AbstractService service;

      /**
       * This lock is used to ensure safe and correct cancellation, it ensures that a new task is
       * not scheduled while a cancel is ongoing.  Also it protects the currentFuture variable to
       * ensure that it is assigned atomically with being scheduled.
       */
      private final ReentrantLock lock = new ReentrantLock();

      /** The future that represents the next execution of this task.*/
      @GuardedBy("lock")
      private Future<Void> currentFuture;

      ReschedulableCallable(AbstractService service, ScheduledExecutorService executor,
          Runnable runnable) {
        this.wrappedRunnable = runnable;
        this.executor = executor;
        this.service = service;
      }

      @Override
      public Void call() throws Exception {
        wrappedRunnable.run();
        reschedule();
        return null;
      }

      /**
       * Atomically reschedules this task and assigns the new future to {@link #currentFuture}.
       */
      public void reschedule() {
        // We reschedule ourselves with a lock held for two reasons. 1. we want to make sure that
        // cancel calls cancel on the correct future. 2. we want to make sure that the assignment
        // to currentFuture doesn't race with itself so that currentFuture is assigned in the
        // correct order.
        lock.lock();
        try {
          if (currentFuture == null || !currentFuture.isCancelled()) {
            final Schedule schedule = CustomScheduler.this.getNextSchedule();
            currentFuture = executor.schedule(this, schedule.delay, schedule.unit);
          }
        } catch (Throwable e) {
          // If an exception is thrown by the subclass then we need to make sure that the service
          // notices and transitions to the FAILED state.  We do it by calling notifyFailed directly
          // because the service does not monitor the state of the future so if the exception is not
          // caught and forwarded to the service the task would stop executing but the service would
          // have no idea.
          service.notifyFailed(e);
        } finally {
          lock.unlock();
        }
      }

      // N.B. Only protect cancel and isCancelled because those are the only methods that are
      // invoked by the AbstractScheduledService.
      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        // Ensure that a task cannot be rescheduled while a cancel is ongoing.
        lock.lock();
        try {
          return currentFuture.cancel(mayInterruptIfRunning);
        } finally {
          lock.unlock();
        }
      }

      @Override
      protected Future<Void> delegate() {
        throw new UnsupportedOperationException("Only cancel is supported by this future");
      }
    }

    @Override
    final Future<?> schedule(AbstractService service, ScheduledExecutorService executor,
        Runnable runnable) {
      ReschedulableCallable task = new ReschedulableCallable(service, executor, runnable);
      task.reschedule();
      return task;
    }

    /**
     * A value object that represents an absolute delay until a task should be invoked.
     *
     * @author Luke Sandberg
     * @since 11.0
     */
    @Beta
    protected static final class Schedule {

      private final long delay;
      private final TimeUnit unit;

      /**
       * @param delay the time from now to delay execution
       * @param unit the time unit of the delay parameter
       */
      public Schedule(long delay, TimeUnit unit) {
        this.delay = delay;
        this.unit = Preconditions.checkNotNull(unit);
      }
    }

    /**
     * Calculates the time at which to next invoke the task.
     *
     * <p>This is guaranteed to be called immediately after the task has completed an iteration and
     * on the same thread as the previous execution of {@link
     * AbstractScheduledService#runOneIteration}.
     *
     * @return a schedule that defines the delay before the next execution.
     */
    protected abstract Schedule getNextSchedule() throws Exception;
  }
}
