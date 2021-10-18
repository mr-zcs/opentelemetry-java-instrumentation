/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.spring.webmvc;

import io.opentelemetry.instrumentation.api.instrumenter.http.CapturedHttpHeaders;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerAttributesExtractor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

final class SpringWebMvcHttpAttributesExtractor
    extends HttpServerAttributesExtractor<HttpServletRequest, HttpServletResponse> {

  SpringWebMvcHttpAttributesExtractor(CapturedHttpHeaders capturedHttpHeaders) {
    super(capturedHttpHeaders);
  }

  @Override
  @Nullable
  protected String method(HttpServletRequest request) {
    return request.getMethod();
  }

  @Override
  protected List<String> requestHeader(HttpServletRequest request, String name) {
    Enumeration<String> headers = request.getHeaders(name);
    return headers == null ? Collections.emptyList() : Collections.list(headers);
  }

  @Override
  @Nullable
  protected Long requestContentLength(
      HttpServletRequest request, @Nullable HttpServletResponse response) {
    return null;
  }

  @Override
  @Nullable
  protected Long requestContentLengthUncompressed(
      HttpServletRequest request, @Nullable HttpServletResponse response) {
    return null;
  }

  @Override
  @Nullable
  protected String flavor(HttpServletRequest request) {
    return request.getProtocol();
  }

  @Override
  @Nullable
  protected Integer statusCode(HttpServletRequest request, HttpServletResponse response) {
    // set in StatusCodeExtractor
    return null;
  }

  @Override
  @Nullable
  protected Long responseContentLength(HttpServletRequest request, HttpServletResponse response) {
    return null;
  }

  @Override
  @Nullable
  protected Long responseContentLengthUncompressed(
      HttpServletRequest request, HttpServletResponse response) {
    return null;
  }

  @Override
  protected List<String> responseHeader(
      HttpServletRequest request, HttpServletResponse response, String name) {
    Collection<String> headers = response.getHeaders(name);
    if (headers == null) {
      return Collections.emptyList();
    }
    if (headers instanceof List) {
      return (List<String>) headers;
    }
    return new ArrayList<>(headers);
  }

  @Override
  @Nullable
  protected String target(HttpServletRequest request) {
    String target = request.getRequestURI();
    String queryString = request.getQueryString();
    if (queryString != null) {
      target += "?" + queryString;
    }
    return target;
  }

  @Override
  @Nullable
  protected String route(HttpServletRequest request) {
    return null;
  }

  @Override
  @Nullable
  protected String scheme(HttpServletRequest request) {
    return request.getScheme();
  }

  @Override
  @Nullable
  protected String serverName(HttpServletRequest request, @Nullable HttpServletResponse response) {
    return null;
  }
}
