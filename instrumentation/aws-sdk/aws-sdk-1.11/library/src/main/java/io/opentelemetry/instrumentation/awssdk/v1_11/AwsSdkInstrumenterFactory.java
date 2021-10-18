/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.awssdk.v1_11;

import com.amazonaws.Request;
import com.amazonaws.Response;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.SpanKindExtractor;
import java.util.Arrays;
import java.util.List;

final class AwsSdkInstrumenterFactory {
  private static final String INSTRUMENTATION_NAME = "io.opentelemetry.aws-sdk-1.11";

  private static final AwsSdkAttributesExtractor attributesExtractor =
      new AwsSdkAttributesExtractor();
  private static final AwsSdkNetAttributesExtractor netAttributesExtractor =
      new AwsSdkNetAttributesExtractor();
  private static final AwsSdkExperimentalAttributesExtractor experimentalAttributesExtractor =
      new AwsSdkExperimentalAttributesExtractor();
  private static final AwsSdkSpanKindExtractor spanKindExtractor = new AwsSdkSpanKindExtractor();

  private static final List<AttributesExtractor<Request<?>, Response<?>>>
      defaultAttributesExtractors = Arrays.asList(attributesExtractor, netAttributesExtractor);
  private static final List<AttributesExtractor<Request<?>, Response<?>>>
      extendedAttributesExtractors =
          Arrays.asList(
              attributesExtractor, netAttributesExtractor, experimentalAttributesExtractor);
  private static final AwsSdkSpanNameExtractor spanName = new AwsSdkSpanNameExtractor();

  static Instrumenter<Request<?>, Response<?>> requestInstrumenter(
      OpenTelemetry openTelemetry, boolean captureExperimentalSpanAttributes) {

    return createInstrumenter(
        openTelemetry,
        captureExperimentalSpanAttributes,
        AwsSdkInstrumenterFactory.spanKindExtractor);
  }

  static Instrumenter<Request<?>, Response<?>> consumerInstrumenter(
      OpenTelemetry openTelemetry, boolean captureExperimentalSpanAttributes) {

    return createInstrumenter(
        openTelemetry, captureExperimentalSpanAttributes, SpanKindExtractor.alwaysConsumer());
  }

  private static Instrumenter<Request<?>, Response<?>> createInstrumenter(
      OpenTelemetry openTelemetry,
      boolean captureExperimentalSpanAttributes,
      SpanKindExtractor<Request<?>> kindExtractor) {
    return Instrumenter.<Request<?>, Response<?>>newBuilder(
            openTelemetry, INSTRUMENTATION_NAME, spanName)
        .addAttributesExtractors(
            captureExperimentalSpanAttributes
                ? extendedAttributesExtractors
                : defaultAttributesExtractors)
        .newInstrumenter(kindExtractor);
  }

  private AwsSdkInstrumenterFactory() {}
}
