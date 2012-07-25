/*
 * Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.googleapis;

import com.google.api.client.http.ExponentialBackOffPolicy;

/**
 * Extension of {@link ExponentialBackOffPolicy} that calls the Media HTTP Uploader call back method
 * before backing off requests.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.7
 * @author rmistry@google.com (Ravi Mistry)
 *
 * @deprecated (scheduled to be removed in 1.11)
 */
@Deprecated
class MediaExponentialBackOffPolicy extends ExponentialBackOffPolicy {

  /** The uploader to callback on if there is a server error. */
  private final MediaHttpUploader uploader;

  MediaExponentialBackOffPolicy(MediaHttpUploader uploader) {
    super();
    this.uploader = uploader;
  }

  /**
   * Gets the number of milliseconds to wait before retrying an HTTP request. If {@link #STOP} is
   * returned, no retries should be made. Calls the Media HTTP Uploader call back method before
   * backing off requests.
   *
   * @return the number of milliseconds to wait when backing off requests, or {@link #STOP} if no
   *         more retries should be made
   */
  @Override
  public long getNextBackOffMillis() {
    try {
      // Call the Media HTTP Uploader to calculate how much data was uploaded before the error and
      // then adjust the HTTP request before the retry.
      uploader.serverErrorCallback();
      return super.getNextBackOffMillis();
    } catch (Exception e) {
      // The call back threw an exception re-raise it.
      throw new RuntimeException(e);
    }
  }
}
