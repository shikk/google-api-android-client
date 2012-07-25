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

package com.google.api.client.auth.oauth2.draft10;

/**
 * Constants for installed (or "native") applications.
 *
 * @since 1.4
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in 1.11) Use
 *             {@code com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants} from <a
 *             href="http://code.google.com/p/google-api-java-client/">google-api-java-client</a>
 */
@Deprecated
public class InstalledApp {

  /**
   * Redirect URI to use for "Out Of Band", meaning that the end-user is given an access code that
   * they must then enter into the installed application.
   */
  public static final String OOB_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

  private InstalledApp() {
  }
}
