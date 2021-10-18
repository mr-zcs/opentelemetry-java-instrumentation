/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.apachedubbo.v2_7.internal;

import io.opentelemetry.instrumentation.apachedubbo.v2_7.DubboRequest;
import io.opentelemetry.instrumentation.api.instrumenter.net.InetSocketAddressNetServerAttributesExtractor;
import java.net.InetSocketAddress;
import javax.annotation.Nullable;
import org.apache.dubbo.rpc.Result;

public final class DubboNetServerAttributesExtractor
    extends InetSocketAddressNetServerAttributesExtractor<DubboRequest, Result> {

  @Override
  @Nullable
  public InetSocketAddress getAddress(DubboRequest request) {
    return request.context().getRemoteAddress();
  }

  @Override
  @Nullable
  public String transport(DubboRequest request) {
    return null;
  }
}
