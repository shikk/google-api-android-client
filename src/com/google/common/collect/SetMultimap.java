/*
 * Copyright (C) 2007 The Guava Authors
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

package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * A {@code Multimap} that cannot hold duplicate key-value pairs. Adding a
 * key-value pair that's already in the multimap has no effect. See the {@link
 * Multimap} documentation for information common to all multimaps.
 *
 * <p>The {@link #get}, {@link #removeAll}, and {@link #replaceValues} methods
 * each return a {@link Set} of values, while {@link #entries} returns a {@code
 * Set} of map entries. Though the method signature doesn't say so explicitly,
 * the map returned by {@link #asMap} has {@code Set} values.
 *
 * <p>If the values corresponding to a single key should be ordered according to
 * a {@link java.util.Comparator} (or the natural order), see the
 * {@link SortedSetMultimap} subinterface.
 *
 * <p>See the Guava User Guide article on <a href=
 * "http://code.google.com/p/guava-libraries/wiki/NewCollectionTypesExplained#Multimap">
 * {@code Multimap}</a>.
 *
 * @author Jared Levy
 * @since 2.0 (imported from Google Collections Library)
 */
@GwtCompatible
public interface SetMultimap<K, V> extends Multimap<K, V> {
  /**
   * {@inheritDoc}
   *
   * <p>Because a {@code SetMultimap} has unique values for a given key, this
   * method returns a {@link Set}, instead of the {@link java.util.Collection}
   * specified in the {@link Multimap} interface.
   */
  @Override
  Set<V> get(@Nullable K key);

  /**
   * {@inheritDoc}
   *
   * <p>Because a {@code SetMultimap} has unique values for a given key, this
   * method returns a {@link Set}, instead of the {@link java.util.Collection}
   * specified in the {@link Multimap} interface.
   */
  @Override
  Set<V> removeAll(@Nullable Object key);

  /**
   * {@inheritDoc}
   *
   * <p>Because a {@code SetMultimap} has unique values for a given key, this
   * method returns a {@link Set}, instead of the {@link java.util.Collection}
   * specified in the {@link Multimap} interface.
   *
   * <p>Any duplicates in {@code values} will be stored in the multimap once.
   */
  @Override
  Set<V> replaceValues(K key, Iterable<? extends V> values);

  /**
   * {@inheritDoc}
   *
   * <p>Because a {@code SetMultimap} has unique values for a given key, this
   * method returns a {@link Set}, instead of the {@link java.util.Collection}
   * specified in the {@link Multimap} interface.
   */
  @Override
  Set<Map.Entry<K, V>> entries();

  /**
   * {@inheritDoc}
   *
   * <p>Though the method signature doesn't say so explicitly, the returned map
   * has {@link Set} values.
   */
  @Override
  Map<K, Collection<V>> asMap();

  /**
   * Compares the specified object to this multimap for equality.
   *
   * <p>Two {@code SetMultimap} instances are equal if, for each key, they
   * contain the same values. Equality does not depend on the ordering of keys
   * or values.
   *
   * <p>An empty {@code SetMultimap} is equal to any other empty {@code
   * Multimap}, including an empty {@code ListMultimap}.
   */
  @Override
  boolean equals(@Nullable Object obj);
}
