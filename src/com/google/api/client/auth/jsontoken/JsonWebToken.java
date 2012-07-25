/*
 * Copyright (c) 2012 Google Inc.
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

package com.google.api.client.auth.jsontoken;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Clock;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;

/**
 * <a href="http://tools.ietf.org/html/draft-jones-json-web-token-07">JSON Web Token (JWT)</a>.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class JsonWebToken {

  /** Header. */
  private final Header header;

  /** Payload. */
  private final Payload payload;

  /**
   * @param header header
   * @param payload payload
   */
  public JsonWebToken(Header header, Payload payload) {
    this.header = Preconditions.checkNotNull(header);
    this.payload = Preconditions.checkNotNull(payload);
  }

  /**
   * Header as specified in <a
   * href="http://tools.ietf.org/html/draft-jones-json-web-token-07#section-5">JWT Header</a>.
   */
  public static class Header extends GenericJson {

    /**
     * Type header parameter used to declare structural information about the JWT or {@code null}
     * for none.
     */
    @Key("typ")
    private String type;

    /**
     * Returns the type header parameter used to declare structural information about the JWT or
     * {@code null} for none.
     */
    public final String getType() {
      return type;
    }

    /**
     * Sets the type header parameter used to declare structural information about the JWT or
     * {@code null} for none.
     */
    public Header setType(String type) {
      this.type = type;
      return this;
    }
  }

  /**
   * Payload as specified in <a
   * href="http://tools.ietf.org/html/draft-jones-json-web-token-07#section-4.1">Reserved Claim
   * Names</a>.
   */
  public static class Payload extends GenericJson {
    /**
     * Clock used for expiration checks.
     */
    private final Clock clock;

    /**
     * Expiration time claim that identifies the expiration time (in seconds) on or after which the
     * token MUST NOT be accepted for processing or {@code null} for none.
     */
    @Key("exp")
    private Long expirationTimeSeconds;

    /**
     * Not before claim that identifies the time (in seconds) before which the token MUST NOT be
     * accepted for processing or {@code null} for none.
     */
    @Key("nbf")
    private Long notBeforeTimeSeconds;

    /**
     * Issued at claim that identifies the time (in seconds) at which the JWT was issued or
     * {@code null} for none.
     */
    @Key("iat")
    private Long issuedAtTimeSeconds;

    /**
     * Issuer claim that identifies the principal that issued the JWT or {@code null} for none.
     */
    @Key("iss")
    private String issuer;

    /**
     * Audience claim that identifies the audience that the JWT is intended for or {@code null} for
     * none.
     */
    @Key("aud")
    private String audience;

    /**
     * Principal claim that identifies the subject of the JWT or {@code null} for none.
     */
    @Key("prn")
    private String principal;

    /**
     * JWT ID claim that provides a unique identifier for the JWT or {@code null} for none.
     */
    @Key("jti")
    private String jwtId;

    /**
     * Type claim that is used to declare a type for the contents of this JWT Claims Set or
     * {@code null} for none.
     */
    @Key("typ")
    private String type;

    /**
     * Constructs a new Payload using default settings.
     */
    public Payload() {
      this(Clock.SYSTEM);
    }

    /**
     * Constructs a new Payload with specific parameters. Primarily used for testing.
     * @param clock Clock to use for expiration checks
     * @since 1.9
     */
    public Payload(Clock clock) {
      this.clock = Preconditions.checkNotNull(clock);
    }

    /**
     * Returns the expiration time (in seconds) claim that identifies the expiration time on or
     * after which the token MUST NOT be accepted for processing or {@code null} for none.
     */
    public Long getExpirationTimeSeconds() {
      return expirationTimeSeconds;
    }

    /**
     * Sets the expiration time claim that identifies the expiration time (in seconds) on or after
     * which the token MUST NOT be accepted for processing or {@code null} for none.
     */
    public Payload setExpirationTimeSeconds(Long expirationTimeSeconds) {
      this.expirationTimeSeconds = expirationTimeSeconds;
      return this;
    }

    /**
     * Returns the not before claim that identifies the time (in seconds) before which the token
     * MUST NOT be accepted for processing or {@code null} for none.
     */
    public Long getNotBeforeTimeSeconds() {
      return notBeforeTimeSeconds;
    }

    /**
     * Sets the not before claim that identifies the time (in seconds) before which the token MUST
     * NOT be accepted for processing or {@code null} for none.
     */
    public Payload setNotBeforeTimeSeconds(Long notBeforeTimeSeconds) {
      this.notBeforeTimeSeconds = notBeforeTimeSeconds;
      return this;
    }

    /**
     * Returns the issued at claim that identifies the time (in seconds) at which the JWT was issued
     * or {@code null} for none.
     */
    public Long getIssuedAtTimeSeconds() {
      return issuedAtTimeSeconds;
    }

    /**
     * Sets the issued at claim that identifies the time (in seconds) at which the JWT was issued or
     * {@code null} for none.
     */
    public Payload setIssuedAtTimeSeconds(Long issuedAtTimeSeconds) {
      this.issuedAtTimeSeconds = issuedAtTimeSeconds;
      return this;
    }

    /**
     * Returns the issuer claim that identifies the principal that issued the JWT or {@code null}
     * for none.
     */
    public String getIssuer() {
      return issuer;
    }

    /**
     * Sets the issuer claim that identifies the principal that issued the JWT or {@code null} for
     * none.
     */
    public Payload setIssuer(String issuer) {
      this.issuer = issuer;
      return this;
    }

    /**
     * Returns the audience claim that identifies the audience that the JWT is intended for or
     * {@code null} for none.
     */
    public String getAudience() {
      return audience;
    }

    /**
     * Sets the audience claim that identifies the audience that the JWT is intended for or
     * {@code null} for none.
     */
    public Payload setAudience(String audience) {
      this.audience = audience;
      return this;
    }

    /**
     * Returns the principal claim that identifies the subject of the JWT or {@code null} for none.
     */
    public String getPrincipal() {
      return principal;
    }

    /**
     * Sets the principal claim that identifies the subject of the JWT or {@code null} for none.
     */
    public Payload setPrincipal(String principal) {
      this.principal = principal;
      return this;
    }

    /**
     * Returns the JWT ID claim that provides a unique identifier for the JWT or {@code null} for
     * none.
     */
    public String getJwtId() {
      return jwtId;
    }

    /**
     * Sets the JWT ID claim that provides a unique identifier for the JWT or {@code null} for none.
     */
    public Payload setJwtId(String jwtId) {
      this.jwtId = jwtId;
      return this;
    }

    /**
     * Returns the type claim that is used to declare a type for the contents of this JWT Claims Set
     * or {@code null} for none.
     */
    public String getType() {
      return type;
    }

    /**
     * Sets the type claim that is used to declare a type for the contents of this JWT Claims Set or
     * {@code null} for none.
     */
    public Payload setType(String type) {
      this.type = type;
      return this;
    }

    /**
     * Returns whether the {@link #getExpirationTimeSeconds} and {@link #getIssuedAtTimeSeconds} are
     * valid relative to the current time, optionally allowing for a clock skew.
     *
     * <p>
     * Default implementation checks that the {@link #getExpirationTimeSeconds() expiration time}
     * and {@link #getIssuedAtTimeSeconds() issued at time} are valid based on the
     * {@link Clock#currentTimeMillis() current time}, allowing for the clock skew. Subclasses may
     * override.
     * </p>
     *
     * @param acceptableTimeSkewSeconds seconds of acceptable clock skew
     */
    public boolean isValidTime(long acceptableTimeSkewSeconds) {
      long now = clock.currentTimeMillis();
      return (expirationTimeSeconds == null
          || now <= (expirationTimeSeconds + acceptableTimeSkewSeconds) * 1000) && (
          issuedAtTimeSeconds == null
          || now >= (issuedAtTimeSeconds - acceptableTimeSkewSeconds) * 1000);
    }
  }

  /**
   * Returns the header.
   *
   * <p>
   * Subclasses may override only to change the return type.
   * </p>
   */
  public Header getHeader() {
    return header;
  }

  /**
   * Returns the payload.
   *
   * <p>
   * Subclasses may override only to change the return type.
   * </p>
   */
  public Payload getPayload() {
    return payload;
  }
}
