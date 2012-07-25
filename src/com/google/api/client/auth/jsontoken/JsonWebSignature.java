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

import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.Key;
import com.google.api.client.util.StringUtils;
import com.google.common.base.Preconditions;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * <a href="http://tools.ietf.org/html/draft-ietf-jose-json-web-signature-00">JSON Web Signature
 * (JWS)</a>.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  public static void printPayload(JsonFactory jsonFactory, String tokenString) throws IOException {
    JsonWebSignature jws = JsonWebSignature.parse(jsonFactory, tokenString);
    System.out.println(jws.getPayload());
  }
 * </pre>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class JsonWebSignature extends JsonWebToken {

  /** Bytes of the signature. */
  private final byte[] signatureBytes;

  /** Bytes of the signature content. */
  private final byte[] signedContentBytes;

  /**
   * @param header header
   * @param payload payload
   * @param signatureBytes bytes of the signature
   * @param signedContentBytes bytes of the signature content
   */
  public JsonWebSignature(
      Header header, Payload payload, byte[] signatureBytes, byte[] signedContentBytes) {
    super(header, payload);
    this.signatureBytes = signatureBytes;
    this.signedContentBytes = signedContentBytes;
  }

  /**
   * Header as specified in <a
   * href="http://tools.ietf.org/html/draft-ietf-jose-json-web-signature-00#section-4.1">Reserved
   * Header Parameter Names</a>.
   */
  public static class Header extends JsonWebToken.Header {

    /**
     * Algorithm header parameter that identifies the cryptographic algorithm used to secure the JWS
     * or {@code null} for none.
     */
    @Key("alg")
    private String algorithm;

    /**
     * JSON Web Key URL header parameter that is an absolute URL that refers to a resource for a set
     * of JSON-encoded public keys, one of which corresponds to the key that was used to digitally
     * sign the JWS or {@code null} for none.
     */
    @Key("jku")
    private String jwkUrl;

    /**
     * Key ID header parameter that is a hint indicating which specific key owned by the signer
     * should be used to validate the digital signature or {@code null} for none.
     */
    @Key("kid")
    private String keyId;

    /**
     * X.509 URL header parameter that is an absolute URL that refers to a resource for the X.509
     * public key certificate or certificate chain corresponding to the key used to digitally sign
     * the JWS or {@code null} for none.
     */
    @Key("x5u")
    private String x509Url;

    /**
     * x.509 certificate thumbprint header parameter that provides a base64url encoded SHA-1
     * thumbprint (a.k.a. digest) of the DER encoding of an X.509 certificate that can be used to
     * match the certificate or {@code null} for none.
     */
    @Key("x5t")
    private String x509Thumbprint;

    @Override
    public Header setType(String type) {
      super.setType(type);
      return this;
    }

    /**
     * Returns the algorithm header parameter that identifies the cryptographic algorithm used to
     * secure the JWS or {@code null} for none.
     */
    public final String getAlgorithm() {
      return algorithm;
    }

    /**
     * Sets the algorithm header parameter that identifies the cryptographic algorithm used to
     * secure the JWS or {@code null} for none.
     */
    public Header setAlgorithm(String algorithm) {
      this.algorithm = algorithm;
      return this;
    }

    /**
     * Returns the JSON Web Key URL header parameter that is an absolute URL that refers to a
     * resource for a set of JSON-encoded public keys, one of which corresponds to the key that was
     * used to digitally sign the JWS or {@code null} for none.
     */
    public String getJwkUrl() {
      return jwkUrl;
    }

    /**
     * Sets the JSON Web Key URL header parameter that is an absolute URL that refers to a resource
     * for a set of JSON-encoded public keys, one of which corresponds to the key that was used to
     * digitally sign the JWS or {@code null} for none.
     */
    public Header setJwkUrl(String jwkUrl) {
      this.jwkUrl = jwkUrl;
      return this;
    }

    /**
     * Returns the key ID header parameter that is a hint indicating which specific key owned by the
     * signer should be used to validate the digital signature or {@code null} for none.
     */
    public String getKeyId() {
      return keyId;
    }

    /**
     * Sets the key ID header parameter that is a hint indicating which specific key owned by the
     * signer should be used to validate the digital signature or {@code null} for none.
     */
    public Header setKeyId(String keyId) {
      this.keyId = keyId;
      return this;
    }

    /**
     * Returns the X.509 URL header parameter that is an absolute URL that refers to a resource for
     * the X.509 public key certificate or certificate chain corresponding to the key used to
     * digitally sign the JWS or {@code null} for none.
     */
    public String getX509Url() {
      return x509Url;
    }

    /**
     * Sets the X.509 URL header parameter that is an absolute URL that refers to a resource for the
     * X.509 public key certificate or certificate chain corresponding to the key used to digitally
     * sign the JWS or {@code null} for none.
     */
    public Header setX509Url(String x509Url) {
      this.x509Url = x509Url;
      return this;
    }

    /**
     * Returns the x.509 certificate thumbprint header parameter that provides a base64url encoded
     * SHA-1 thumbprint (a.k.a. digest) of the DER encoding of an X.509 certificate that can be used
     * to match the certificate or {@code null} for none.
     */
    public String getX509Thumbprint() {
      return x509Thumbprint;
    }

    /**
     * Sets the x.509 certificate thumbprint header parameter that provides a base64url encoded
     * SHA-1 thumbprint (a.k.a. digest) of the DER encoding of an X.509 certificate that can be used
     * to match the certificate or {@code null} for none.
     */
    public Header setX509Thumbprint(String x509Thumbprint) {
      this.x509Thumbprint = x509Thumbprint;
      return this;
    }
  }

  @Override
  public Header getHeader() {
    return (Header) super.getHeader();
  }

  /** Returns the bytes of the signature. */
  public final byte[] getSignatureBytes() {
    return signatureBytes;
  }

  /** Returns the bytes of the signature content. */
  public final byte[] getSignedContentBytes() {
    return signedContentBytes;
  }

  /**
   * Parses the given JWS token string and returns the parsed {@link JsonWebSignature}.
   *
   * @param jsonFactory JSON factory
   * @param tokenString JWS token string
   * @return parsed JWS
   */
  public static JsonWebSignature parse(JsonFactory jsonFactory, String tokenString)
      throws IOException {
    return parser(jsonFactory).parse(tokenString);
  }

  /** Returns a new instance of a JWS parser. */
  public static Parser parser(JsonFactory jsonFactory) {
    return new Parser(jsonFactory);
  }

  /**
   * JWS parser.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   */
  public static final class Parser {

    /** JSON factory. */
    private final JsonFactory jsonFactory;

    /** Header class to use for parsing. */
    private Class<? extends Header> headerClass = Header.class;

    /** Payload class to use for parsing. */
    private Class<? extends Payload> payloadClass = Payload.class;

    /**
     * @param jsonFactory JSON factory
     */
    public Parser(JsonFactory jsonFactory) {
      this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    }

    /** Returns the header class to use for parsing. */
    public Class<? extends Header> getHeaderClass() {
      return headerClass;
    }

    /** Sets the header class to use for parsing. */
    public Parser setHeaderClass(Class<? extends Header> headerClass) {
      this.headerClass = headerClass;
      return this;
    }

    /** Returns the payload class to use for parsing. */
    public Class<? extends Payload> getPayloadClass() {
      return payloadClass;
    }

    /** Sets the payload class to use for parsing. */
    public Parser setPayloadClass(Class<? extends Payload> payloadClass) {
      this.payloadClass = payloadClass;
      return this;
    }

    /** Returns the JSON factory. */
    public JsonFactory getJsonFactory() {
      return jsonFactory;
    }

    /**
     * Parses a JWS token into a parsed {@link JsonWebSignature}.
     * @param tokenString JWS token string
     * @return parsed {@link JsonWebSignature}
     */
    public JsonWebSignature parse(String tokenString) throws IOException {
      // split on the dots
      int firstDot = tokenString.indexOf('.');
      Preconditions.checkArgument(firstDot != -1);
      byte[] headerBytes = Base64.decodeBase64(tokenString.substring(0, firstDot));
      int secondDot = tokenString.indexOf('.', firstDot + 1);
      Preconditions.checkArgument(secondDot != -1);
      Preconditions.checkArgument(tokenString.indexOf('.', secondDot + 1) == -1);
      // decode the bytes
      byte[] payloadBytes =
          Base64.decodeBase64(tokenString.substring(firstDot + 1, secondDot));
      byte[] signatureBytes = Base64.decodeBase64(tokenString.substring(secondDot + 1));
      byte[] signedContentBytes = StringUtils.getBytesUtf8(tokenString.substring(0, secondDot));
      // parse the header and payload
      Header header =
          jsonFactory.fromInputStream(new ByteArrayInputStream(headerBytes), headerClass);
      Preconditions.checkArgument(header.getAlgorithm() != null);
      Payload payload =
          jsonFactory.fromInputStream(new ByteArrayInputStream(payloadBytes), payloadClass);
      return new JsonWebSignature(header, payload, signatureBytes, signedContentBytes);
    }
  }
}
