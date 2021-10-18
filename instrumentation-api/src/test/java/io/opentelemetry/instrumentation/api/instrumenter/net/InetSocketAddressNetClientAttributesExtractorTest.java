/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter.net;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InetSocketAddressNetClientAttributesExtractorTest {

  private final InetSocketAddressNetClientAttributesExtractor<InetSocketAddress, InetSocketAddress>
      extractor =
          new InetSocketAddressNetClientAttributesExtractor<
              InetSocketAddress, InetSocketAddress>() {
            @Override
            public InetSocketAddress getAddress(
                InetSocketAddress request, InetSocketAddress response) {
              return response;
            }

            @Override
            public String transport(InetSocketAddress request, InetSocketAddress response) {
              return SemanticAttributes.NetTransportValues.IP_TCP;
            }
          };

  @Test
  void noInetSocketAddress() {
    AttributesBuilder attributes = Attributes.builder();
    extractor.onEnd(attributes, null, null, null);
    assertThat(attributes.build())
        .containsOnly(
            entry(SemanticAttributes.NET_TRANSPORT, SemanticAttributes.NetTransportValues.IP_TCP));
  }

  @Test
  void fullAddress() {
    // given
    InetSocketAddress request = new InetSocketAddress("github.com", 123);
    assertThat(request.getAddress().getHostAddress()).isNotNull();

    InetSocketAddress response = new InetSocketAddress("api.github.com", 456);
    assertThat(request.getAddress().getHostAddress()).isNotNull();

    // when
    AttributesBuilder startAttributes = Attributes.builder();
    extractor.onStart(startAttributes, request);

    AttributesBuilder endAttributes = Attributes.builder();
    extractor.onEnd(endAttributes, request, response, null);

    // then
    assertThat(startAttributes.build()).isEmpty();

    assertThat(endAttributes.build())
        .containsOnly(
            entry(SemanticAttributes.NET_TRANSPORT, SemanticAttributes.NetTransportValues.IP_TCP),
            entry(SemanticAttributes.NET_PEER_IP, response.getAddress().getHostAddress()),
            entry(SemanticAttributes.NET_PEER_NAME, "api.github.com"),
            entry(SemanticAttributes.NET_PEER_PORT, 456L));
  }

  @Test
  void unresolved() {
    // given
    InetSocketAddress request = InetSocketAddress.createUnresolved("github.com", 123);
    assertThat(request.getAddress()).isNull();

    InetSocketAddress response = InetSocketAddress.createUnresolved("api.github.com", 456);
    assertThat(request.getAddress()).isNull();

    // when
    AttributesBuilder startAttributes = Attributes.builder();
    extractor.onStart(startAttributes, request);

    AttributesBuilder endAttributes = Attributes.builder();
    extractor.onEnd(endAttributes, request, response, null);

    // then
    assertThat(startAttributes.build()).isEmpty();

    assertThat(endAttributes.build())
        .containsOnly(
            entry(SemanticAttributes.NET_TRANSPORT, SemanticAttributes.NetTransportValues.IP_TCP),
            entry(SemanticAttributes.NET_PEER_NAME, "api.github.com"),
            entry(SemanticAttributes.NET_PEER_PORT, 456L));
  }
}
