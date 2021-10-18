/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.awslambda.v1_0;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.OpenTelemetrySdkAutoConfiguration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TracingSqsEventWrapper extends TracingSqsEventHandler {

  private final WrappedLambda wrappedLambda;
  private final Method targetMethod;

  public TracingSqsEventWrapper() {
    this(OpenTelemetrySdkAutoConfiguration.initialize(), WrappedLambda.fromConfiguration());
  }

  // Visible for testing
  TracingSqsEventWrapper(OpenTelemetrySdk openTelemetrySdk, WrappedLambda wrappedLambda) {
    super(openTelemetrySdk, WrapperConfiguration.flushTimeout());
    this.wrappedLambda = wrappedLambda;
    this.targetMethod = wrappedLambda.getRequestTargetMethod();
  }

  @Override
  protected void handleEvent(SQSEvent sqsEvent, Context context) {
    Object[] parameters =
        LambdaParameters.toArray(targetMethod, sqsEvent, context, (event, clazz) -> event);
    try {
      targetMethod.invoke(wrappedLambda.getTargetObject(), parameters);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Method is inaccessible", e);
    } catch (InvocationTargetException e) {
      throw (e.getCause() instanceof RuntimeException
          ? (RuntimeException) e.getCause()
          : new IllegalStateException(e.getTargetException()));
    }
  }
}
