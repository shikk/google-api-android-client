/*
 * Copyright (C) 2008 The Guava Authors
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
import com.google.common.collect.Maps.EntryTransformer;

import java.lang.reflect.Array;
import java.util.NavigableMap;
import java.util.SortedMap;

/**
 * Methods factored out so that they can be emulated differently in GWT.
 *
 * @author Hayward Chan
 */
@GwtCompatible(emulated = true)
class Platform {
  /**
   * Clone the given array using {@link Object#clone()}.  It is factored out so
   * that it can be emulated in GWT.
   */
  static <T> T[] clone(T[] array) {
    return array.clone();
  }

  /**
   * Returns a new array of the given length with the same type as a reference
   * array.
   *
   * @param reference any array of the desired type
   * @param length the length of the new array
   */
  static <T> T[] newArray(T[] reference, int length) {
    Class<?> type = reference.getClass().getComponentType();

    // the cast is safe because
    // result.getClass() == reference.getClass().getComponentType()
    @SuppressWarnings("unchecked")
    T[] result = (T[]) Array.newInstance(type, length);
    return result;
  }

  /**
   * Configures the given map maker to use weak keys, if possible; does nothing
   * otherwise (i.e., in GWT). This is sometimes acceptable, when only
   * server-side code could generate enough volume that reclamation becomes
   * important.
   */
  static MapMaker tryWeakKeys(MapMaker mapMaker) {
    return mapMaker.weakKeys();
  }

  static <K, V1, V2> SortedMap<K, V2> mapsTransformEntriesSortedMap(
      SortedMap<K, V1> fromMap,
      EntryTransformer<? super K, ? super V1, V2> transformer) {
    return (fromMap instanceof NavigableMap)
        ? Maps.transformEntries((NavigableMap<K, V1>) fromMap, transformer)
        : Maps.transformEntriesIgnoreNavigable(fromMap, transformer);
  }

  private Platform() {}
}
