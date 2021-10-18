/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.kafkaclients;

import static io.opentelemetry.javaagent.instrumentation.kafkaclients.KafkaSingletons.consumerProcessInstrumenter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.javaagent.bootstrap.kafka.KafkaClientsConsumerProcessTracing;
import io.opentelemetry.javaagent.bootstrap.kafka.KafkaClientsConsumerProcessWrapper;
import java.util.Iterator;
import javax.annotation.Nullable;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class TracingIterator<K, V>
    implements Iterator<ConsumerRecord<K, V>>,
        KafkaClientsConsumerProcessWrapper<Iterator<ConsumerRecord<K, V>>> {
  private final Iterator<ConsumerRecord<K, V>> delegateIterator;
  private final Context parentContext;

  /*
   * Note: this may potentially create problems if this iterator is used from different threads. But
   * at the moment we cannot do much about this.
   */
  @Nullable private ConsumerRecord<?, ?> currentRequest;
  @Nullable private Context currentContext;
  @Nullable private Scope currentScope;

  private TracingIterator(
      Iterator<ConsumerRecord<K, V>> delegateIterator, @Nullable SpanContext receiveSpanContext) {
    this.delegateIterator = delegateIterator;

    // use the receive CONSUMER span as parent if it's available
    Context parentContext = Context.current();
    if (receiveSpanContext != null) {
      parentContext = parentContext.with(Span.wrap(receiveSpanContext));
    }
    this.parentContext = parentContext;
  }

  public static <K, V> Iterator<ConsumerRecord<K, V>> wrap(
      Iterator<ConsumerRecord<K, V>> delegateIterator, @Nullable SpanContext receiveSpanContext) {
    if (KafkaClientsConsumerProcessTracing.wrappingEnabled()) {
      return new TracingIterator<>(delegateIterator, receiveSpanContext);
    }
    return delegateIterator;
  }

  @Override
  public boolean hasNext() {
    closeScopeAndEndSpan();
    return delegateIterator.hasNext();
  }

  @Override
  public ConsumerRecord<K, V> next() {
    // in case they didn't call hasNext()...
    closeScopeAndEndSpan();

    ConsumerRecord<K, V> next = delegateIterator.next();
    if (next != null && consumerProcessInstrumenter().shouldStart(parentContext, next)) {
      currentRequest = next;
      currentContext = consumerProcessInstrumenter().start(parentContext, currentRequest);
      currentScope = currentContext.makeCurrent();
    }
    return next;
  }

  private void closeScopeAndEndSpan() {
    if (currentScope != null) {
      currentScope.close();
      consumerProcessInstrumenter().end(currentContext, currentRequest, null, null);
      currentScope = null;
      currentRequest = null;
      currentContext = null;
    }
  }

  @Override
  public void remove() {
    delegateIterator.remove();
  }

  @Override
  public Iterator<ConsumerRecord<K, V>> unwrap() {
    return delegateIterator;
  }
}
