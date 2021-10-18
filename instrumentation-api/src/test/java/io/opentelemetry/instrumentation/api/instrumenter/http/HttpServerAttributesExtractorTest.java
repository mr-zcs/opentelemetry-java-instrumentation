/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter.http;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.entry;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HttpServerAttributesExtractorTest {

  static class TestHttpServerAttributesExtractor
      extends HttpServerAttributesExtractor<Map<String, String>, Map<String, String>> {

    TestHttpServerAttributesExtractor(CapturedHttpHeaders capturedHttpHeaders) {
      super(capturedHttpHeaders);
    }

    @Override
    protected String method(Map<String, String> request) {
      return request.get("method");
    }

    @Override
    protected String target(Map<String, String> request) {
      return request.get("target");
    }

    @Override
    protected String route(Map<String, String> request) {
      return request.get("route");
    }

    @Override
    protected String scheme(Map<String, String> request) {
      return request.get("scheme");
    }

    @Override
    protected String serverName(Map<String, String> request, Map<String, String> response) {
      return request.get("serverName");
    }

    @Override
    protected List<String> requestHeader(Map<String, String> request, String name) {
      String values = request.get("header." + name);
      return values == null ? emptyList() : asList(values.split(","));
    }

    @Override
    protected Long requestContentLength(Map<String, String> request, Map<String, String> response) {
      String value = request.get("requestContentLength");
      return value == null ? null : Long.parseLong(value);
    }

    @Override
    protected Long requestContentLengthUncompressed(
        Map<String, String> request, Map<String, String> response) {
      String value = request.get("requestContentLengthUncompressed");
      return value == null ? null : Long.parseLong(value);
    }

    @Override
    protected Integer statusCode(Map<String, String> request, Map<String, String> response) {
      String value = response.get("statusCode");
      return value == null ? null : Integer.parseInt(value);
    }

    @Override
    protected String flavor(Map<String, String> request) {
      return request.get("flavor");
    }

    @Override
    protected Long responseContentLength(
        Map<String, String> request, Map<String, String> response) {
      String value = response.get("responseContentLength");
      return value == null ? null : Long.parseLong(value);
    }

    @Override
    protected Long responseContentLengthUncompressed(
        Map<String, String> request, Map<String, String> response) {
      String value = response.get("responseContentLengthUncompressed");
      return value == null ? null : Long.parseLong(value);
    }

    @Override
    protected List<String> responseHeader(
        Map<String, String> request, Map<String, String> response, String name) {
      String values = response.get("header." + name);
      return values == null ? emptyList() : asList(values.split(","));
    }
  }

  @Test
  void normal() {
    Map<String, String> request = new HashMap<>();
    request.put("method", "POST");
    request.put("url", "http://github.com");
    request.put("target", "/repositories/1");
    request.put("scheme", "https");
    request.put("requestContentLength", "10");
    request.put("requestContentLengthUncompressed", "11");
    request.put("flavor", "http/2");
    request.put("route", "/repositories/{id}");
    request.put("serverName", "server");
    request.put("header.user-agent", "okhttp 3.x");
    request.put("header.host", "github.com");
    request.put("header.forwarded", "for=1.1.1.1");
    request.put("header.custom-request-header", "123,456");

    Map<String, String> response = new HashMap<>();
    response.put("statusCode", "202");
    response.put("responseContentLength", "20");
    response.put("responseContentLengthUncompressed", "21");
    response.put("header.custom-response-header", "654,321");

    TestHttpServerAttributesExtractor extractor =
        new TestHttpServerAttributesExtractor(
            CapturedHttpHeaders.create(
                singletonList("Custom-Request-Header"), singletonList("Custom-Response-Header")));

    AttributesBuilder attributes = Attributes.builder();
    extractor.onStart(attributes, request);
    assertThat(attributes.build())
        .containsOnly(
            entry(SemanticAttributes.HTTP_FLAVOR, "http/2"),
            entry(SemanticAttributes.HTTP_METHOD, "POST"),
            entry(SemanticAttributes.HTTP_SCHEME, "https"),
            entry(SemanticAttributes.HTTP_HOST, "github.com"),
            entry(SemanticAttributes.HTTP_TARGET, "/repositories/1"),
            entry(SemanticAttributes.HTTP_USER_AGENT, "okhttp 3.x"),
            entry(SemanticAttributes.HTTP_ROUTE, "/repositories/{id}"),
            entry(SemanticAttributes.HTTP_CLIENT_IP, "1.1.1.1"),
            entry(
                AttributeKey.stringArrayKey("http.request.header.custom_request_header"),
                asList("123", "456")));

    extractor.onEnd(attributes, request, response, null);
    assertThat(attributes.build())
        .containsOnly(
            entry(SemanticAttributes.HTTP_METHOD, "POST"),
            entry(SemanticAttributes.HTTP_SCHEME, "https"),
            entry(SemanticAttributes.HTTP_HOST, "github.com"),
            entry(SemanticAttributes.HTTP_TARGET, "/repositories/1"),
            entry(SemanticAttributes.HTTP_USER_AGENT, "okhttp 3.x"),
            entry(SemanticAttributes.HTTP_ROUTE, "/repositories/{id}"),
            entry(SemanticAttributes.HTTP_CLIENT_IP, "1.1.1.1"),
            entry(
                AttributeKey.stringArrayKey("http.request.header.custom_request_header"),
                asList("123", "456")),
            entry(SemanticAttributes.HTTP_SERVER_NAME, "server"),
            entry(SemanticAttributes.HTTP_REQUEST_CONTENT_LENGTH, 10L),
            entry(SemanticAttributes.HTTP_REQUEST_CONTENT_LENGTH_UNCOMPRESSED, 11L),
            entry(SemanticAttributes.HTTP_FLAVOR, "http/2"),
            entry(SemanticAttributes.HTTP_STATUS_CODE, 202L),
            entry(SemanticAttributes.HTTP_RESPONSE_CONTENT_LENGTH, 20L),
            entry(SemanticAttributes.HTTP_RESPONSE_CONTENT_LENGTH_UNCOMPRESSED, 21L),
            entry(
                AttributeKey.stringArrayKey("http.response.header.custom_response_header"),
                asList("654", "321")));
  }

  @Test
  void extractClientIpFromX_Forwarded_For() {
    Map<String, String> request = new HashMap<>();
    request.put("header.x-forwarded-for", "1.1.1.1");

    TestHttpServerAttributesExtractor extractor =
        new TestHttpServerAttributesExtractor(CapturedHttpHeaders.empty());

    AttributesBuilder attributes = Attributes.builder();
    extractor.onStart(attributes, request);
    assertThat(attributes.build())
        .containsOnly(entry(SemanticAttributes.HTTP_CLIENT_IP, "1.1.1.1"));

    extractor.onEnd(attributes, request, null, null);
    assertThat(attributes.build())
        .containsOnly(entry(SemanticAttributes.HTTP_CLIENT_IP, "1.1.1.1"));
  }
}
