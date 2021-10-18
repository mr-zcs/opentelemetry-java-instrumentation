/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.awslambda.v1_0.internal;

import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.CLOUD_ACCOUNT_ID;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.FAAS_ID;
import static io.opentelemetry.semconv.trace.attributes.SemanticAttributes.FAAS_EXECUTION;

import com.amazonaws.services.lambda.runtime.Context;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.awslambda.v1_0.AwsLambdaRequest;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.checkerframework.checker.nullness.qual.Nullable;

class AwsLambdaFunctionAttributesExtractor
    implements AttributesExtractor<AwsLambdaRequest, Object> {

  @Nullable private static final MethodHandle GET_FUNCTION_ARN;

  static {
    MethodHandles.Lookup lookup = MethodHandles.publicLookup();
    MethodHandle getFunctionArn;
    try {
      getFunctionArn =
          lookup.findVirtual(
              Context.class, "getInvokedFunctionArn", MethodType.methodType(String.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      getFunctionArn = null;
    }
    GET_FUNCTION_ARN = getFunctionArn;
  }

  // cached accountId value
  private volatile String accountId;

  @Override
  public void onStart(AttributesBuilder attributes, AwsLambdaRequest request) {
    Context context = request.getAwsContext();
    set(attributes, FAAS_EXECUTION, context.getAwsRequestId());
    set(attributes, FAAS_ID, getFunctionArn(context));
    set(attributes, CLOUD_ACCOUNT_ID, getAccountId(getFunctionArn(context)));
  }

  @Override
  public void onEnd(
      AttributesBuilder attributes,
      AwsLambdaRequest request,
      @Nullable Object response,
      @Nullable Throwable error) {}

  @Nullable
  private static String getFunctionArn(Context context) {
    if (GET_FUNCTION_ARN == null) {
      return null;
    }
    try {
      return (String) GET_FUNCTION_ARN.invoke(context);
    } catch (Throwable throwable) {
      return null;
    }
  }

  @Nullable
  private String getAccountId(@Nullable String arn) {
    if (arn == null) {
      return null;
    }
    if (accountId == null) {
      synchronized (this) {
        if (accountId == null) {
          String[] arnParts = arn.split(":");
          if (arnParts.length >= 5) {
            accountId = arnParts[4];
          }
        }
      }
    }
    return accountId;
  }
}
