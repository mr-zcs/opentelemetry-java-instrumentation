/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.netty.v3_8.server;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerMetrics;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpSpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpSpanStatusExtractor;
import org.jboss.netty.handler.codec.http.HttpResponse;

final class NettyServerSingletons {

  private static final Instrumenter<HttpRequestAndChannel, HttpResponse> INSTRUMENTER;

  static {
    NettyHttpServerAttributesExtractor httpServerAttributesExtractor =
        new NettyHttpServerAttributesExtractor();

    INSTRUMENTER =
        Instrumenter.<HttpRequestAndChannel, HttpResponse>newBuilder(
                GlobalOpenTelemetry.get(),
                "io.opentelemetry.netty-3.8",
                HttpSpanNameExtractor.create(httpServerAttributesExtractor))
            .setSpanStatusExtractor(HttpSpanStatusExtractor.create(httpServerAttributesExtractor))
            .addAttributesExtractor(httpServerAttributesExtractor)
            .addAttributesExtractor(new NettyNetServerAttributesExtractor())
            .addRequestMetrics(HttpServerMetrics.get())
            .newServerInstrumenter(new NettyHeadersGetter());
  }

  public static Instrumenter<HttpRequestAndChannel, HttpResponse> instrumenter() {
    return INSTRUMENTER;
  }

  private NettyServerSingletons() {}
}
