/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import javax.annotation.Nullable;

/**
 * Extractor of <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/trace/semantic_conventions/span-general.md#general-network-connection-attributes">Network
 * attributes</a> from a {@link InetSocketAddress}. Most network libraries will provide access to a
 * {@link InetSocketAddress} so this is a convenient alternative to {@link
 * NetServerAttributesExtractor}. There is no meaning to implement both in the same instrumentation.
 */
public abstract class InetSocketAddressNetServerAttributesExtractor<REQUEST, RESPONSE>
    extends NetServerAttributesExtractor<REQUEST, RESPONSE> {

  @Nullable
  public abstract InetSocketAddress getAddress(REQUEST request);

  @Override
  @Nullable
  public final String peerName(REQUEST request) {
    InetSocketAddress address = getAddress(request);
    if (address == null) {
      return null;
    }
    if (address.getAddress() != null) {
      return address.getAddress().getHostName();
    }
    return address.getHostString();
  }

  @Override
  @Nullable
  public final Integer peerPort(REQUEST request) {
    InetSocketAddress address = getAddress(request);
    if (address == null) {
      return null;
    }
    return address.getPort();
  }

  @Override
  @Nullable
  public final String peerIp(REQUEST request) {
    InetSocketAddress address = getAddress(request);
    if (address == null) {
      return null;
    }
    InetAddress remoteAddress = address.getAddress();
    if (remoteAddress != null) {
      return remoteAddress.getHostAddress();
    }
    return null;
  }
}
