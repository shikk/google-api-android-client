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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializes HTTP request content into an output stream.
 *
 * <p>
 * Implementations don't need to be thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public interface HttpContent {

  /** Returns the content length or less than zero if not known. */
  long getLength() throws IOException;

  /**
   * Returns the content encoding (for example {@code "gzip"}) or {@code null} for none.
   */
  String getEncoding();

  /** Returns the content type or {@code null} for none. */
  String getType();

  /**
   * Writes the content to the given output stream.
   *
   * <p>
   * The recommendation for implementations is that they should not close the output stream. Callers
   * should not assume whether or not the output stream has been closed. Implementations that do not
   * close the output stream should flush it at the end of the method.
   * </p>
   */
  void writeTo(OutputStream out) throws IOException;

  /**
   * Returns whether or not retry is supported on this content type.
   *
   * @since 1.4
   */
  boolean retrySupported();
}
