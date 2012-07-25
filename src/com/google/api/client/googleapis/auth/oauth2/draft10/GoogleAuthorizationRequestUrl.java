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

package com.google.api.client.googleapis.auth.oauth2.draft10;

import com.google.api.client.auth.oauth2.draft10.AuthorizationRequestUrl;
import com.google.api.client.auth.oauth2.draft10.AuthorizationResponse;
import com.google.api.client.util.Key;

/**
 * Google extension to the OAuth 2.0 (draft 10) URL builder for an authorization web page to allow
 * the end user to authorize the application to access their protected resources.
 * <p>
 * Use {@link AuthorizationResponse} to parse the redirect response after the end user grants/denies
 * the request.
 * </p>
 * <p>
 * Sample usage for a web application:
 *
 * <pre>
 * <code>
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    GoogleAuthorizationRequestUrl builder =
        new GoogleAuthorizationRequestUrl(CLIENT_ID, REDIRECT_URL, SCOPE);
    response.sendRedirect(builder.build());
    return;
  }
 * </code>
 * </pre>
 *
 * @since 1.4
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in 1.11) Use
 *         {@link com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl} or
 *         {@link com.google.api.client.googleapis.auth.oauth2.GoogleBrowserClientRequestUrl} or
 *         {@link com.google.api.client.auth.oauth2.AuthorizationRequestUrl}
 */
@Deprecated
public class GoogleAuthorizationRequestUrl extends AuthorizationRequestUrl {

  /** Authorization server URL for end-user authorization. */
  public static final String AUTHORIZATION_SERVER_URL = "https://accounts.google.com/o/oauth2/auth";

  /**
   * [OPTIONAL] {@code "force"} to force the approval UI to show or {@code "auto"} to request
   * auto-approval when possible ({@code "auto"} is the default if {@code null}).
   */
  @Key("approval_prompt")
  private String approvalPrompt;

  /**
   * [OPTIONAL] {@code "offline"} to request offline access from the user or {@code "online"} to
   * request online access ({@code "online"} is the default if {@code null}).
   */
  @Key("access_type")
  private String accessType;

  public GoogleAuthorizationRequestUrl() {
    super(AUTHORIZATION_SERVER_URL);
  }

  /**
   * @param clientId client identifier
   * @param redirectUri Absolute URI to which the authorization server will redirect the user-agent
   *        to when the end-user authorization step is completed
   * @param scope scope of the access request expressed as a list of space-delimited strings. If the
   *        value contains multiple space-delimited strings, their order does not matter, and each
   *        string adds an additional access range to the requested scope.
   */
  public GoogleAuthorizationRequestUrl(String clientId, String redirectUri, String scope) {
    super(AUTHORIZATION_SERVER_URL, clientId);
    this.redirectUri = redirectUri;
    this.scope = scope;
  }

  /**
   * [OPTIONAL] {@code "force"} to force the approval UI to show or {@code "auto"} to request
   * auto-approval when possible ({@code "auto"} is the default if {@code null}).
   *
   * @since 1.6
   */
  public final String getApprovalPrompt() {
    return approvalPrompt;
  }

  /**
   * [OPTIONAL] {@code "force"} to force the approval UI to show or {@code "auto"} to request
   * auto-approval when possible ({@code "auto"} is the default if {@code null}).
   *
   * <p>
   * Subclasses may override the return value by calling super.
   * </p>
   *
   * @since 1.6
   */
  public GoogleAuthorizationRequestUrl setApprovalPrompt(String approvalPrompt) {
    this.approvalPrompt = approvalPrompt;
    return this;
  }

  /**
   * [OPTIONAL] {@code "offline"} to request offline access from the user or {@code "online"} to
   * request online access ({@code "online"} is the default if {@code null}).
   *
   * @since 1.6
   */
  public final String getAccessType() {
    return accessType;
  }

  /**
   * [OPTIONAL] {@code "offline"} to request offline access from the user or {@code "online"} to
   * request online access ({@code "online"} is the default if {@code null}).
   *
   * <p>
   * Subclasses may override the return value by calling super.
   * </p>
   *
   * @since 1.6
   */
  public GoogleAuthorizationRequestUrl setAccessType(String accessType) {
    this.accessType = accessType;
    return this;
  }
}
