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

package com.google.api.client.http;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Serializes MIME Multipart/Related content as specified by <a
 * href="http://tools.ietf.org/html/rfc2387">RFC 2387: The MIME Multipart/Related Content-type</a>.
 * <p>
 * Limitations:
 * <ul>
 * <li>No support of parameters other than {@code "boundary"}</li>
 * <li>No support for specifying headers for each content part</li>
 * <li>The content type of each part is required, so {@link HttpContent#getType()} must not be
 * {@code null}</li>
 * </ul>
 * </p>
 * <p>
 * Use {@link #forRequest(HttpRequest)} to construct. For example:
 *
 * <pre><code>
  static void setMediaWithMetadataContent(
      HttpRequest request, AtomContent atomContent, InputStreamContent imageContent) {
    MultipartRelatedContent.forRequest(request, atomContent, imageContent);
  }
 * </code></pre>
 * </p>
 *
 * <p>
 * {@link #writeTo} can be called multiple times for Multipart/Related content. Parts must not
 * close the output stream in {@link #writeTo}.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.1
 * @author Yaniv Inbar
 */
public final class MultipartRelatedContent extends AbstractHttpContent {
  /**
   * Collection of HTTP content parts.
   *
   * <p>
   * By default, it is an empty list.
   * </p>
   */
  private final Collection<HttpContent> parts;

  private static final byte[] CR_LF = "\r\n".getBytes();
  private static final byte[] CONTENT_TYPE = "Content-Type: ".getBytes();
  private static final byte[] CONTENT_TRANSFER_ENCODING =
      "Content-Transfer-Encoding: binary".getBytes();
  private static final byte[] TWO_DASHES = "--".getBytes();

  /**
   * @param firstPart first HTTP content part
   * @param otherParts other HTTP content parts
   * @since 1.5
   */
  public MultipartRelatedContent(HttpContent firstPart, HttpContent... otherParts) {
    super(new HttpMediaType("multipart/related").setParameter("boundary", "END_OF_PART"));
    List<HttpContent> parts = new ArrayList<HttpContent>(otherParts.length + 1);
    parts.add(firstPart);
    parts.addAll(Arrays.asList(otherParts));
    this.parts = parts;
  }

  /**
   * Sets this multi-part content as the content for the given HTTP request, and set the
   * {@link HttpHeaders#setMimeVersion(String) MIME version header} to {@code "1.0"}.
   *
   * @param request HTTP request
   * @since 1.5
   */
  public void forRequest(HttpRequest request) {
    request.setContent(this);
    request.getHeaders().setMimeVersion("1.0");
  }

  public void writeTo(OutputStream out) throws IOException {
    byte[] boundaryBytes = getBoundary().getBytes();
    out.write(TWO_DASHES);
    out.write(boundaryBytes);
    for (HttpContent part : parts) {
      String contentType = part.getType();
      if (contentType != null) {
        byte[] typeBytes = contentType.getBytes();
        out.write(CR_LF);
        out.write(CONTENT_TYPE);
        out.write(typeBytes);
      }
      out.write(CR_LF);
      if (!isTextBasedContentType(contentType)) {
        out.write(CONTENT_TRANSFER_ENCODING);
        out.write(CR_LF);
      }
      out.write(CR_LF);
      part.writeTo(out);
      out.write(CR_LF);
      out.write(TWO_DASHES);
      out.write(boundaryBytes);
    }
    out.write(TWO_DASHES);
    out.flush();
  }

  @Override
  public long computeLength() throws IOException {
    byte[] boundaryBytes = getBoundary().getBytes();
    long result = TWO_DASHES.length * 2 + boundaryBytes.length;
    for (HttpContent part : parts) {
      long length = part.getLength();
      if (length < 0) {
        return -1;
      }
      String contentType = part.getType();
      if (contentType != null) {
        byte[] typeBytes = contentType.getBytes();
        result += CR_LF.length + CONTENT_TYPE.length + typeBytes.length;
      }
      if (!isTextBasedContentType(contentType)) {
        result += CONTENT_TRANSFER_ENCODING.length + CR_LF.length;
      }
      result += CR_LF.length * 3 + length + TWO_DASHES.length + boundaryBytes.length;
    }
    return result;
  }

  @Override
  public boolean retrySupported() {
    for (HttpContent onePart : parts) {
      if (!onePart.retrySupported()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public MultipartRelatedContent setMediaType(HttpMediaType mediaType) {
    super.setMediaType(mediaType);
    return this;
  }

  /**
   * Returns the boundary string to use.
   *
   * @since 1.5
   */
  public String getBoundary() {
    return getMediaType().getParameter("boundary");
  }

  /**
   * Sets the boundary string to use.
   *
   * <p>
   * Defaults to {@code "END_OF_PART"}.
   * </p>
   *
   * @since 1.5
   */
  public MultipartRelatedContent setBoundary(String boundary) {
    getMediaType().setParameter("boundary", Preconditions.checkNotNull(boundary));
    return this;
  }

  /**
   * Returns the HTTP content parts.
   *
   * @since 1.5
   */
  public Collection<HttpContent> getParts() {
    return Collections.unmodifiableCollection(parts);
  }

  /**
   * Returns whether the given content type is text rather than binary data.
   *
   * @param contentType content type or {@code null}
   * @return whether it is not {@code null} and text-based
   */
  private static boolean isTextBasedContentType(String contentType) {
    if (contentType == null) {
      return false;
    }
    HttpMediaType hmt = new HttpMediaType(contentType);
    return hmt.getType().equals("text") || hmt.getType().equals("application");
  }
}
