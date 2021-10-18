/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.tooling.field;

import static io.opentelemetry.javaagent.tooling.field.GeneratedVirtualFieldNames.getVirtualFieldImplementationClassName;

import io.opentelemetry.instrumentation.api.field.VirtualField;
import io.opentelemetry.instrumentation.api.internal.RuntimeVirtualFieldSupplier;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class RuntimeFieldBasedImplementationSupplier
    implements RuntimeVirtualFieldSupplier.VirtualFieldSupplier {

  @Override
  public <U extends T, T, F> VirtualField<U, F> find(Class<T> type, Class<F> fieldType) {
    try {
      String virtualFieldImplClassName =
          getVirtualFieldImplementationClassName(type.getName(), fieldType.getName());
      Class<?> contextStoreClass = Class.forName(virtualFieldImplClassName, false, null);
      Method method = contextStoreClass.getMethod("getVirtualField", Class.class, Class.class);
      return (VirtualField<U, F>) method.invoke(null, type, fieldType);
    } catch (ClassNotFoundException exception) {
      throw new IllegalStateException("VirtualField not found", exception);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
      throw new IllegalStateException("Failed to get VirtualField", exception);
    }
  }
}
