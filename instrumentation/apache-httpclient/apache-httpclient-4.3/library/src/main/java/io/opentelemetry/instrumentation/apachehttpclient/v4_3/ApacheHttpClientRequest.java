/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.apachehttpclient.v4_3;

import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApacheHttpClientRequest {

  private static final Logger logger = LoggerFactory.getLogger(ApacheHttpClientRequest.class);

  @Nullable private final URI uri;

  private final HttpRequest delegate;

  ApacheHttpClientRequest(@Nullable HttpHost httpHost, HttpRequest httpRequest) {
    URI calculatedUri = getUri(httpRequest);
    if (calculatedUri != null && httpHost != null) {
      uri = getCalculatedUri(httpHost, calculatedUri);
    } else {
      uri = calculatedUri;
    }
    delegate = httpRequest;
  }

  /** Returns the actual {@link HttpRequest} being executed by the client. */
  public HttpRequest getDelegate() {
    return delegate;
  }

  List<String> getHeader(String name) {
    return headersToList(delegate.getHeaders(name));
  }

  // minimize memory overhead by not using streams
  static List<String> headersToList(Header[] headers) {
    if (headers.length == 0) {
      return Collections.emptyList();
    }
    List<String> headersList = new ArrayList<>(headers.length);
    for (int i = 0; i < headers.length; ++i) {
      headersList.set(i, headers[i].getValue());
    }
    return headersList;
  }

  void setHeader(String name, String value) {
    delegate.setHeader(name, value);
  }

  String getMethod() {
    return delegate.getRequestLine().getMethod();
  }

  @Nullable
  String getUrl() {
    return uri != null ? uri.toString() : null;
  }

  @Nullable
  String getFlavor() {
    ProtocolVersion protocolVersion = delegate.getProtocolVersion();
    String protocol = protocolVersion.getProtocol();
    if (!protocol.equals("HTTP")) {
      return null;
    }
    int major = protocolVersion.getMajor();
    int minor = protocolVersion.getMinor();
    if (major == 1 && minor == 0) {
      return SemanticAttributes.HttpFlavorValues.HTTP_1_0;
    }
    if (major == 1 && minor == 1) {
      return SemanticAttributes.HttpFlavorValues.HTTP_1_1;
    }
    if (major == 2 && minor == 0) {
      return SemanticAttributes.HttpFlavorValues.HTTP_2_0;
    }
    logger.debug("unexpected http protocol version: {}", protocolVersion);
    return null;
  }

  @Nullable
  String getPeerName() {
    return uri != null ? uri.getHost() : null;
  }

  @Nullable
  Integer getPeerPort() {
    if (uri == null) {
      return null;
    }
    int port = uri.getPort();
    if (port != -1) {
      return port;
    }
    switch (uri.getScheme()) {
      case "http":
        return 80;
      case "https":
        return 443;
      default:
        logger.debug("no default port mapping for scheme: {}", uri.getScheme());
        return null;
    }
  }

  @Nullable
  private static URI getUri(HttpRequest httpRequest) {
    try {
      // this can be relative or absolute
      return new URI(httpRequest.getRequestLine().getUri());
    } catch (URISyntaxException e) {
      logger.debug(e.getMessage(), e);
      return null;
    }
  }

  @Nullable
  private static URI getCalculatedUri(HttpHost httpHost, URI uri) {
    try {
      return new URI(
          httpHost.getSchemeName(),
          null,
          httpHost.getHostName(),
          httpHost.getPort(),
          uri.getPath(),
          uri.getQuery(),
          uri.getFragment());
    } catch (URISyntaxException e) {
      logger.debug(e.getMessage(), e);
      return null;
    }
  }
}
