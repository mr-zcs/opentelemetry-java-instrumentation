/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.akkahttp.client;

import akka.http.scaladsl.model.HttpRequest;
import akka.http.scaladsl.model.HttpResponse;
import io.opentelemetry.instrumentation.api.instrumenter.net.NetClientAttributesExtractor;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.checkerframework.checker.nullness.qual.Nullable;

class AkkaHttpNetAttributesExtractor
    extends NetClientAttributesExtractor<HttpRequest, HttpResponse> {
  @Override
  public String transport(HttpRequest httpRequest, @Nullable HttpResponse httpResponse) {
    return SemanticAttributes.NetTransportValues.IP_TCP;
  }

  @Override
  public String peerName(HttpRequest httpRequest, @Nullable HttpResponse httpResponse) {
    return httpRequest.uri().authority().host().address();
  }

  @Override
  public Integer peerPort(HttpRequest httpRequest, @Nullable HttpResponse httpResponse) {
    return httpRequest.uri().authority().port();
  }

  @Override
  public @Nullable String peerIp(HttpRequest httpRequest, @Nullable HttpResponse httpResponse) {
    return null;
  }
}
