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

package com.google.api.client.testing.http;

import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.StringUtils;
import com.google.common.base.Preconditions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock for {@link LowLevelHttpResponse}.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.3
 */
public class MockLowLevelHttpResponse extends LowLevelHttpResponse {

  /** Input stream content of HTTP response or {@code null} by default. */
  private InputStream content;

  /** Content type of HTTP response or {@code null} by default. */
  private String contentType;

  /** Status code of HTTP response or {@code 200} by default. */
  private int statusCode = 200;

  /** HTTP reason phrase or {@code null} for none. */
  private String reasonPhrase;

  /** List of header names of HTTP response (empty array list by default). */
  private List<String> headerNames = new ArrayList<String>();

  /** List of header values of HTTP response (empty array list by default). */
  private List<String> headerValues = new ArrayList<String>();

  /** Content encoding or {@code null} for none. */
  private String contentEncoding;

  /** Content length or {@code -1} if unknown. */
  private long contentLength = -1;

  /**
   * Adds a header to the response.
   *
   * @param name header name
   * @param value header value
   */
  public void addHeader(String name, String value) {
    headerNames.add(Preconditions.checkNotNull(name));
    headerValues.add(Preconditions.checkNotNull(value));
  }

  /**
   * Sets the response content to the given content string.
   *
   * @param stringContent content string or {@code null} for none
   */
  public MockLowLevelHttpResponse setContent(String stringContent) {
    if (stringContent == null) {
      content = null;
      setContentLength(0);
    } else {
      byte[] bytes = StringUtils.getBytesUtf8(stringContent);
      content = new ByteArrayInputStream(bytes);
      setContentLength(bytes.length);
    }
    return this;
  }

  @Override
  public InputStream getContent() throws IOException {
    return content;
  }

  @Override
  public String getContentEncoding() {
    return contentEncoding;
  }

  @Override
  public long getContentLength() {
    return contentLength;
  }

  @Override
  public final String getContentType() {
    return contentType;
  }

  @Override
  public int getHeaderCount() {
    return headerNames.size();
  }

  @Override
  public String getHeaderName(int index) {
    return headerNames.get(index);
  }

  @Override
  public String getHeaderValue(int index) {
    return headerValues.get(index);
  }

  @Override
  public String getReasonPhrase() {
    return reasonPhrase;
  }

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public String getStatusLine() {
    StringBuilder buf = new StringBuilder(statusCode);
    if (reasonPhrase != null) {
      buf.append(reasonPhrase);
    }
    return buf.toString();
  }

  /**
   * Returns the list of header names of HTTP response.
   *
   * @since 1.5
   */
  public final List<String> getHeaderNames() {
    return headerNames;
  }

  /**
   * Sets the list of header names of HTTP response.
   *
   * <p>
   * Default value is an empty list.
   * </p>
   *
   * @since 1.5
   */
  public MockLowLevelHttpResponse setHeaderNames(List<String> headerNames) {
    this.headerNames = Preconditions.checkNotNull(headerNames);
    return this;
  }

  /**
   * Returns the list of header values of HTTP response.
   *
   * <p>
   * Default value is an empty list.
   * </p>
   *
   * @since 1.5
   */
  public final List<String> getHeaderValues() {
    return headerValues;
  }

  /**
   * Sets the list of header values of HTTP response.
   *
   * @since 1.5
   */
  public MockLowLevelHttpResponse setHeaderValues(List<String> headerValues) {
    this.headerValues = Preconditions.checkNotNull(headerValues);
    return this;
  }

  /**
   * Sets the input stream content of HTTP response or {@code null} for none.
   *
   * @since 1.5
   */
  public MockLowLevelHttpResponse setContent(InputStream content) {
    this.content = content;
    return this;
  }

  /**
   * Sets the content type of HTTP response or {@code null} for none.
   *
   * @since 1.5
   */
  public MockLowLevelHttpResponse setContentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  /**
   * Sets the content encoding or {@code null} for none.
   *
   * @since 1.5
   */
  public MockLowLevelHttpResponse setContentEncoding(String contentEncoding) {
    this.contentEncoding = contentEncoding;
    return this;
  }

  /**
   * Sets the content length or {@code -1} for unknown.
   *
   * <p>
   * By default it is {@code -1}.
   * </p>
   *
   * <p>
   * Warning: in prior version {@code 0} was the default, but now the default is {@code -1}.
   * </p>
   *
   * @since 1.5
   */
  public MockLowLevelHttpResponse setContentLength(long contentLength) {
    this.contentLength = contentLength;
    Preconditions.checkArgument(contentLength >= -1);
    return this;
  }

  /**
   * Sets the status code of HTTP response.
   *
   * <p>
   * Default value is {@code 200}.
   * </p>
   *
   * @since 1.5
   */
  public MockLowLevelHttpResponse setStatusCode(int statusCode) {
    this.statusCode = statusCode;
    Preconditions.checkArgument(statusCode >= 0);
    return this;
  }

  /**
   * Sets the HTTP reason phrase or {@code null} for none.
   *
   * @since 1.6
   */
  public MockLowLevelHttpResponse setReasonPhrase(String reasonPhrase) {
    this.reasonPhrase = reasonPhrase;
    return this;
  }
}
