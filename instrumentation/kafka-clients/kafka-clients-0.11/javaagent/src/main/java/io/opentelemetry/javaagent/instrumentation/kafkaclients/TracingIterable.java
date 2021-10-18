/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.kafkaclients;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.javaagent.bootstrap.kafka.KafkaClientsConsumerProcessTracing;
import io.opentelemetry.javaagent.bootstrap.kafka.KafkaClientsConsumerProcessWrapper;
import java.util.Iterator;
import javax.annotation.Nullable;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class TracingIterable<K, V>
    implements Iterable<ConsumerRecord<K, V>>,
        KafkaClientsConsumerProcessWrapper<Iterable<ConsumerRecord<K, V>>> {
  private final Iterable<ConsumerRecord<K, V>> delegate;
  @Nullable private final SpanContext receiveSpanContext;
  private boolean firstIterator = true;

  protected TracingIterable(
      Iterable<ConsumerRecord<K, V>> delegate, @Nullable SpanContext receiveSpanContext) {
    this.delegate = delegate;
    this.receiveSpanContext = receiveSpanContext;
  }

  public static <K, V> Iterable<ConsumerRecord<K, V>> wrap(
      Iterable<ConsumerRecord<K, V>> delegate, @Nullable SpanContext receiveSpanContext) {
    if (KafkaClientsConsumerProcessTracing.wrappingEnabled()) {
      return new TracingIterable<>(delegate, receiveSpanContext);
    }
    return delegate;
  }

  @Override
  public Iterator<ConsumerRecord<K, V>> iterator() {
    Iterator<ConsumerRecord<K, V>> it;
    // We should only return one iterator with tracing.
    // However, this is not thread-safe, but usually the first (hopefully only) traversal of
    // ConsumerRecords is performed in the same thread that called poll()
    if (firstIterator) {
      it = TracingIterator.wrap(delegate.iterator(), receiveSpanContext);
      firstIterator = false;
    } else {
      it = delegate.iterator();
    }

    return it;
  }

  @Override
  public Iterable<ConsumerRecord<K, V>> unwrap() {
    return delegate;
  }
}
