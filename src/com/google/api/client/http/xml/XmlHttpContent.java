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

package com.google.api.client.http.xml;

import com.google.api.client.http.HttpMediaType;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.common.base.Preconditions;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Serializes XML HTTP content based on the data key/value mapping object for an item.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
 * <code>
  static void setContent(HttpRequest request, XmlNamespaceDictionary namespaceDictionary,
      String elementName, Object data) {
    request.setContent(new XmlHttpContent(namespaceDictionary, elementName, data));
  }
 * </code>
 * </pre>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class XmlHttpContent extends AbstractXmlHttpContent {

  /**
   * XML namespace dictionary.
   *
   * @param namespaceDictionary XML namespace dictionary
   * @param elementName XML element local name, optionally prefixed by its namespace alias, for
   *        example {@code "atom:entry"}
   * @param data Key/value pair data
   * @since 1.5
   */
  public XmlHttpContent(
      XmlNamespaceDictionary namespaceDictionary, String elementName, Object data) {
    super(namespaceDictionary);
    this.elementName = Preconditions.checkNotNull(elementName);
    this.data = Preconditions.checkNotNull(data);
  }

  /**
   * XML element local name, optionally prefixed by its namespace alias, for example {@code
   * "atom:entry"}.
   */
  private final String elementName;

  /** Key/value pair data. */
  private final Object data;

  /**
   * {@inheritDoc}
   *
   * @deprecated (scheduled to be removed in 1.11) Use {@link #setMediaType(HttpMediaType)} instead.
   */
  @Deprecated
  @Override
  public XmlHttpContent setType(String type) {
    super.setType(type);
    return this;
  }

  @Override
  public final void writeTo(XmlSerializer serializer) throws IOException {
    getNamespaceDictionary().serialize(serializer, elementName, data);
  }

  @Override
  public XmlHttpContent setMediaType(HttpMediaType mediaType) {
    super.setMediaType(mediaType);
    return this;
  }

  /**
   * Returns the XML element local name, optionally prefixed by its namespace alias, for example
   * {@code "atom:entry"}.
   *
   * @since 1.5
   */
  public final String getElementName() {
    return elementName;
  }

  /**
   * Returns the key/value pair data.
   *
   * @since 1.5
   */
  public final Object getData() {
    return data;
  }
}
