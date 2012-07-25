/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.http;

import com.google.api.client.util.ObjectParser;

import java.io.IOException;

/**
 * Parses HTTP response content into an data class of key/value pairs.
 *
 * <p>
 * Implementations should normally be thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in 1.11) Use {@link ObjectParser} instead.
 */
@Deprecated
public interface HttpParser {

  /** Returns the content type. */
  String getContentType();

  /**
   * Parses the given HTTP response into a new instance of the the given data class of key/value
   * pairs.
   * <p>
   * How the parsing is performed is not restricted by this interface, and is instead defined by the
   * concrete implementation. Implementations should check
   * {@link HttpResponse#isSuccessStatusCode()} to know whether they are parsing a success or error
   * response.
   */
  <T> T parse(HttpResponse response, Class<T> dataClass) throws IOException;
}
