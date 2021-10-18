/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.netty.v3_8.server;

import io.opentelemetry.context.propagation.TextMapGetter;
import javax.annotation.Nullable;

final class NettyHeadersGetter implements TextMapGetter<HttpRequestAndChannel> {

  @Override
  public Iterable<String> keys(HttpRequestAndChannel requestAndChannel) {
    return requestAndChannel.request().headers().names();
  }

  @Nullable
  @Override
  public String get(@Nullable HttpRequestAndChannel requestAndChannel, String s) {
    return requestAndChannel.request().headers().get(s);
  }
}
