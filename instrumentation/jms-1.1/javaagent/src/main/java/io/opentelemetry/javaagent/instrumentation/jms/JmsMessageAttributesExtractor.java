/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.jms;

import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesExtractor;
import javax.annotation.Nullable;
import javax.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsMessageAttributesExtractor
    extends MessagingAttributesExtractor<MessageWithDestination, Void> {
  private static final Logger logger = LoggerFactory.getLogger(JmsMessageAttributesExtractor.class);

  private final MessageOperation operation;

  public JmsMessageAttributesExtractor(MessageOperation operation) {
    this.operation = operation;
  }

  @Override
  public MessageOperation operation() {
    return operation;
  }

  @Nullable
  @Override
  protected String system(MessageWithDestination messageWithDestination) {
    return "jms";
  }

  @Nullable
  @Override
  protected String destinationKind(MessageWithDestination messageWithDestination) {
    return messageWithDestination.destinationKind();
  }

  @Nullable
  @Override
  protected String destination(MessageWithDestination messageWithDestination) {
    return messageWithDestination.destinationName();
  }

  @Override
  protected boolean temporaryDestination(MessageWithDestination messageWithDestination) {
    return messageWithDestination.isTemporaryDestination();
  }

  @Nullable
  @Override
  protected String protocol(MessageWithDestination messageWithDestination) {
    return null;
  }

  @Nullable
  @Override
  protected String protocolVersion(MessageWithDestination messageWithDestination) {
    return null;
  }

  @Nullable
  @Override
  protected String url(MessageWithDestination messageWithDestination) {
    return null;
  }

  @Nullable
  @Override
  protected String conversationId(MessageWithDestination messageWithDestination) {
    try {
      return messageWithDestination.message().getJMSCorrelationID();
    } catch (JMSException e) {
      logger.debug("Failure getting JMS correlation id", e);
      return null;
    }
  }

  @Nullable
  @Override
  protected Long messagePayloadSize(MessageWithDestination messageWithDestination) {
    return null;
  }

  @Nullable
  @Override
  protected Long messagePayloadCompressedSize(MessageWithDestination messageWithDestination) {
    return null;
  }

  @Nullable
  @Override
  protected String messageId(MessageWithDestination messageWithDestination, Void unused) {
    try {
      return messageWithDestination.message().getJMSMessageID();
    } catch (JMSException e) {
      logger.debug("Failure getting JMS message id", e);
      return null;
    }
  }
}
