/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.tomcat.common;

import io.opentelemetry.instrumentation.api.instrumenter.net.NetServerAttributesExtractor;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import javax.annotation.Nullable;
import org.apache.coyote.ActionCode;
import org.apache.coyote.Request;
import org.apache.coyote.Response;

public class TomcatNetAttributesExtractor extends NetServerAttributesExtractor<Request, Response> {

  @Override
  @Nullable
  public String transport(Request request) {
    return SemanticAttributes.NetTransportValues.IP_TCP;
  }

  @Override
  @Nullable
  public String peerName(Request request) {
    request.action(ActionCode.REQ_HOST_ATTRIBUTE, request);
    return request.remoteHost().toString();
  }

  @Override
  @Nullable
  public Integer peerPort(Request request) {
    request.action(ActionCode.REQ_REMOTEPORT_ATTRIBUTE, request);
    return request.getRemotePort();
  }

  @Override
  @Nullable
  public String peerIp(Request request) {
    request.action(ActionCode.REQ_HOST_ADDR_ATTRIBUTE, request);
    return request.remoteAddr().toString();
  }
}
