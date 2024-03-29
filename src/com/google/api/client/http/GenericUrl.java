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

import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.api.client.util.escape.CharEscapers;
import com.google.api.client.util.escape.Escaper;
import com.google.api.client.util.escape.PercentEscaper;
import com.google.common.base.Preconditions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * URL builder in which the query parameters are specified as generic data key/value pairs, based on
 * the specification <a href="http://tools.ietf.org/html/rfc3986">RFC 3986: Uniform Resource
 * Identifier (URI)</a>.
 *
 * <p>
 * The query parameters are specified with the data key name as the parameter name, and the data
 * value as the parameter value. Subclasses can declare fields for known query parameters using the
 * {@link Key} annotation. {@code null} parameter names are not allowed, but {@code null} query
 * values are allowed.
 * </p>
 *
 * <p>
 * Query parameter values are parsed using {@link UrlEncodedParser#parse(String, Object)}.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class GenericUrl extends GenericData {

  private static final Escaper URI_FRAGMENT_ESCAPER =
      new PercentEscaper("=&-_.!~*'()@:$,;/?:", false);

  /** Scheme (lowercase), for example {@code "https"}. */
  private String scheme;

  /** Host, for example {@code "www.google.com"}. */
  private String host;

  /** Port number or {@code -1} if undefined, for example {@code 443}. */
  private int port = -1;

  /**
   * Decoded path component by parts with each part separated by a {@code '/'} or {@code null} for
   * none, for example {@code "/m8/feeds/contacts/default/full"} is represented by {@code "", "m8",
   *"feeds", "contacts", "default", "full"}.
   * <p>
   * Use {@link #appendRawPath(String)} to append to the path, which ensures that no extra slash is
   * added.
   * </p>
   */
  private List<String> pathParts;

  /** Fragment component or {@code null} for none. */
  private String fragment;

  public GenericUrl() {
  }

  /**
   * Constructs from an encoded URL.
   * <p>
   * Any known query parameters with pre-defined fields as data keys will be parsed based on their
   * data type. Any unrecognized query parameter will always be parsed as a string.
   *
   * @param encodedUrl encoded URL, including any existing query parameters that should be parsed
   * @throws IllegalArgumentException if URL has a syntax error
   */
  public GenericUrl(String encodedUrl) {
    URI uri;
    try {
      uri = new URI(encodedUrl);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    scheme = uri.getScheme().toLowerCase();
    host = uri.getHost();
    port = uri.getPort();
    pathParts = toPathParts(uri.getRawPath());
    fragment = uri.getFragment();
    String query = uri.getRawQuery();
    if (query != null) {
      UrlEncodedParser.parse(query, this);
    }
  }

  @Override
  public int hashCode() {
    // TODO(yanivi): optimize?
    return build().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj) || !(obj instanceof GenericUrl)) {
      return false;
    }
    GenericUrl other = (GenericUrl) obj;
    // TODO(yanivi): optimize?
    return build().equals(other.toString());
  }

  @Override
  public String toString() {
    return build();
  }

  @Override
  public GenericUrl clone() {
    GenericUrl result = (GenericUrl) super.clone();
    if (pathParts != null) {
      result.pathParts = new ArrayList<String>(pathParts);
    }
    return result;
  }


  /**
   * Returns the scheme (lowercase), for example {@code "https"}.
   *
   * @since 1.5
   */
  public final String getScheme() {
    return scheme;
  }

  /**
   * Sets the scheme (lowercase), for example {@code "https"}.
   *
   * @since 1.5
   */
  public final void setScheme(String scheme) {
    this.scheme = Preconditions.checkNotNull(scheme);
  }

  /**
   * Returns the host, for example {@code "www.google.com"}.
   *
   * @since 1.5
   */
  public String getHost() {
    return host;
  }

  /**
   * Sets the host, for example {@code "www.google.com"}.
   *
   * @since 1.5
   */
  public final void setHost(String host) {
    this.host = Preconditions.checkNotNull(host);
  }

  /**
   * Returns the port number or {@code -1} if undefined, for example {@code 443}.
   *
   * @since 1.5
   */
  public int getPort() {
    return port;
  }

  /**
   * Returns the port number or {@code -1} if undefined, for example {@code 443}.
   *
   * @since 1.5
   */
  public final void setPort(int port) {
    Preconditions.checkArgument(port >= -1, "expected port >= -1");
    this.port = port;
  }

  /**
   * Sets the decoded path component by parts with each part separated by a {@code '/'} or
   * {@code null} for none.
   *
   * @since 1.5
   */
  public List<String> getPathParts() {
    return pathParts;
  }

  /**
   * Sets the decoded path component by parts with each part separated by a {@code '/'} or
   * {@code null} for none.
   *
   * <p>
   * For example {@code "/m8/feeds/contacts/default/full"} is represented by {@code "", "m8",
   *"feeds", "contacts", "default", "full"}.
   * </p>
   *
   * <p>
   * Use {@link #appendRawPath(String)} to append to the path, which ensures that no extra slash is
   * added.
   * </p>
   *
   * @since 1.5
   */
  public void setPathParts(List<String> pathParts) {
    this.pathParts = pathParts;
  }

  /**
   * Returns the fragment component or {@code null} for none.
   *
   * @since 1.5
   */
  public String getFragment() {
    return fragment;
  }

  /**
   * Sets the fragment component or {@code null} for none.
   *
   * @since 1.5
   */
  public final void setFragment(String fragment) {
    this.fragment = fragment;
  }

  /**
   * Constructs the string representation of the URL, including the path specified by
   * {@link #pathParts} and the query parameters specified by this generic URL.
   */
  public final String build() {
    return buildAuthority() + buildRelativeUrl();
  }

  /**
   * Constructs the portion of the URL containing the scheme, host and port.
   *
   * <p>
   * For the URL {@code "http://example.com/something?action=add"} this method would return
   * {@code "http://example.com"}.
   * </p>
   *
   * @return scheme://host[:port]
   * @since 1.9
   */
  public final String buildAuthority() {
    // scheme, host, port
    StringBuilder buf = new StringBuilder();
    buf.append(Preconditions.checkNotNull(scheme));
    buf.append("://");
    buf.append(Preconditions.checkNotNull(host));
    int port = this.port;
    if (port != -1) {
      buf.append(':').append(port);
    }
    return buf.toString();
  }

  /**
   * Constructs the portion of the URL beginning at the rooted path.
   *
   * <p>
   * For the URL {@code "http://example.com/something?action=add"} this method would return
   * {@code "/something?action=add"}.
   * </p>
   *
   * @return path with with leading '/' if the path is non-empty, query parameters and fragment
   * @since 1.9
   */
  public final String buildRelativeUrl() {
    StringBuilder buf = new StringBuilder();
    if (pathParts != null) {
      appendRawPathFromParts(buf);
    }
    addQueryParams(entrySet(), buf);

    // URL fragment
    String fragment = this.fragment;
    if (fragment != null) {
      buf.append('#').append(URI_FRAGMENT_ESCAPER.escape(fragment));
    }
    return buf.toString();
  }

  /**
   * Returns the first query parameter value for the given query parameter name.
   *
   * @param name query parameter name
   * @return first query parameter value
   */
  public Object getFirst(String name) {
    Object value = get(name);
    if (value instanceof Collection<?>) {
      @SuppressWarnings("unchecked")
      Collection<Object> collectionValue = (Collection<Object>) value;
      Iterator<Object> iterator = collectionValue.iterator();
      return iterator.hasNext() ? iterator.next() : null;
    }
    return value;
  }

  /**
   * Returns all query parameter values for the given query parameter name.
   *
   * @param name query parameter name
   * @return unmodifiable collection of query parameter values (possibly empty)
   */
  public Collection<Object> getAll(String name) {
    Object value = get(name);
    if (value == null) {
      return Collections.emptySet();
    }
    if (value instanceof Collection<?>) {
      @SuppressWarnings("unchecked")
      Collection<Object> collectionValue = (Collection<Object>) value;
      return Collections.unmodifiableCollection(collectionValue);
    }
    return Collections.singleton(value);
  }

  /**
   * Returns the raw encoded path computed from the {@link #pathParts}.
   *
   * @return raw encoded path computed from the {@link #pathParts} or {@code null} if
   *         {@link #pathParts} is {@code null}
   */
  public String getRawPath() {
    List<String> pathParts = this.pathParts;
    if (pathParts == null) {
      return null;
    }
    StringBuilder buf = new StringBuilder();
    appendRawPathFromParts(buf);
    return buf.toString();
  }

  /**
   * Sets the {@link #pathParts} from the given raw encoded path.
   *
   * @param encodedPath raw encoded path or {@code null} to set {@link #pathParts} to {@code null}
   */
  public void setRawPath(String encodedPath) {
    pathParts = toPathParts(encodedPath);
  }

  /**
   * Appends the given raw encoded path to the current {@link #pathParts}, setting field only if it
   * is {@code null} or empty.
   * <p>
   * The last part of the {@link #pathParts} is merged with the first part of the path parts
   * computed from the given encoded path. Thus, if the current raw encoded path is {@code "a"}, and
   * the given encoded path is {@code "b"}, then the resulting raw encoded path is {@code "ab"}.
   * </p>
   *
   * @param encodedPath raw encoded path or {@code null} to ignore
   */
  public void appendRawPath(String encodedPath) {
    if (encodedPath != null && encodedPath.length() != 0) {
      List<String> appendedPathParts = toPathParts(encodedPath);
      if (pathParts == null || pathParts.isEmpty()) {
        this.pathParts = appendedPathParts;
      } else {
        int size = pathParts.size();
        pathParts.set(size - 1, pathParts.get(size - 1) + appendedPathParts.get(0));
        pathParts.addAll(appendedPathParts.subList(1, appendedPathParts.size()));
      }
    }
  }

  /**
   * Returns the decoded path parts for the given encoded path.
   *
   * @param encodedPath slash-prefixed encoded path, for example
   *        {@code "/m8/feeds/contacts/default/full"}
   * @return decoded path parts, with each part assumed to be preceded by a {@code '/'}, for example
   *         {@code "", "m8", "feeds", "contacts", "default", "full"}, or {@code null} for
   *         {@code null} or {@code ""} input
   */
  public static List<String> toPathParts(String encodedPath) {
    if (encodedPath == null || encodedPath.length() == 0) {
      return null;
    }
    List<String> result = new ArrayList<String>();
    int cur = 0;
    boolean notDone = true;
    while (notDone) {
      int slash = encodedPath.indexOf('/', cur);
      notDone = slash != -1;
      String sub;
      if (notDone) {
        sub = encodedPath.substring(cur, slash);
      } else {
        sub = encodedPath.substring(cur);
      }
      result.add(CharEscapers.decodeUri(sub));
      cur = slash + 1;
    }
    return result;
  }

  private void appendRawPathFromParts(StringBuilder buf) {
    int size = pathParts.size();
    for (int i = 0; i < size; i++) {
      String pathPart = pathParts.get(i);
      if (i != 0) {
        buf.append('/');
      }
      if (pathPart.length() != 0) {
        buf.append(CharEscapers.escapeUriPath(pathPart));
      }
    }
  }

  /**
   * Adds query parameters from the provided entrySet into the buffer.
   */
  static void addQueryParams(Set<Entry<String, Object>> entrySet, StringBuilder buf) {
    // (similar to UrlEncodedContent)
    boolean first = true;
    for (Map.Entry<String, Object> nameValueEntry : entrySet) {
      Object value = nameValueEntry.getValue();
      if (value != null) {
        String name = CharEscapers.escapeUriQuery(nameValueEntry.getKey());
        if (value instanceof Collection<?>) {
          Collection<?> collectionValue = (Collection<?>) value;
          for (Object repeatedValue : collectionValue) {
            first = appendParam(first, buf, name, repeatedValue);
          }
        } else {
          first = appendParam(first, buf, name, value);
        }
      }
    }
  }

  private static boolean appendParam(boolean first, StringBuilder buf, String name, Object value) {
    if (first) {
      first = false;
      buf.append('?');
    } else {
      buf.append('&');
    }
    buf.append(name);
    String stringValue = CharEscapers.escapeUriQuery(value.toString());
    if (stringValue.length() != 0) {
      buf.append('=').append(stringValue);
    }
    return first;
  }
}
