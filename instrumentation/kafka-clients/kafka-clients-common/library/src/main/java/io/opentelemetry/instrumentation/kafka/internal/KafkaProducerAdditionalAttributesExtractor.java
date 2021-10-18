/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.kafka.internal;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import javax.annotation.Nullable;
import org.apache.kafka.clients.producer.ProducerRecord;

public final class KafkaProducerAdditionalAttributesExtractor
    implements AttributesExtractor<ProducerRecord<?, ?>, Void> {
  @Override
  public void onStart(AttributesBuilder attributes, ProducerRecord<?, ?> producerRecord) {
    Integer partition = producerRecord.partition();
    if (partition != null) {
      set(attributes, SemanticAttributes.MESSAGING_KAFKA_PARTITION, partition.longValue());
    }
    if (producerRecord.value() == null) {
      set(attributes, SemanticAttributes.MESSAGING_KAFKA_TOMBSTONE, true);
    }
  }

  @Override
  public void onEnd(
      AttributesBuilder attributes,
      ProducerRecord<?, ?> producerRecord,
      @Nullable Void unused,
      @Nullable Throwable error) {}
}
