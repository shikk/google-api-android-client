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

package com.google.api.client.http.xml.atom;

import com.google.api.client.http.xml.XmlHttpParser;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.xml.XmlObjectParser;
import com.google.api.client.xml.atom.Atom;

/**
 * Atom XML HTTP parser into an data class of key/value pairs.
 *
 * <p>
 * It overrides the {@link #getContentType} to {@link Atom#CONTENT_TYPE}.
 * </p>
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  static void setParser(HttpRequest request, XmlNamespaceDictionary namespaceDictionary) {
    request.addParser(new AtomParser(namespaceDictionary));
  }
 * </pre>
 *
 * @since 1.4
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in 1.11) Content-Type is no longer stored inside of the
 *             Parser. Use an {@link XmlObjectParser} instead.
 */
@Deprecated
public final class AtomParser extends XmlHttpParser {

  /**
   * @param namespaceDictionary XML namespace dictionary
   * @since 1.5
   */
  public AtomParser(XmlNamespaceDictionary namespaceDictionary) {
    super(namespaceDictionary, Atom.CONTENT_TYPE);
  }
}
