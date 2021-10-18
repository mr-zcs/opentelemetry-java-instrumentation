/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter.http;

import javax.annotation.Nullable;

final class ForwarderHeaderParser {

  // VisibleForTesting
  @Nullable
  static String extractForwarded(String forwarded) {
    int start = forwarded.toLowerCase().indexOf("for=");
    if (start < 0) {
      return null;
    }
    start += 4; // start is now the index after for=
    if (start >= forwarded.length() - 1) { // the value after for= must not be empty
      return null;
    }
    return extractIpAddress(forwarded, start);
  }

  // VisibleForTesting
  @Nullable
  static String extractForwardedFor(String forwarded) {
    return extractIpAddress(forwarded, 0);
  }

  // from https://www.rfc-editor.org/rfc/rfc7239
  //  "Note that IPv6 addresses may not be quoted in
  //   X-Forwarded-For and may not be enclosed by square brackets, but they
  //   are quoted and enclosed in square brackets in Forwarded"
  // and also (applying to Forwarded but not X-Forwarded-For)
  //  "It is important to note that an IPv6 address and any nodename with
  //   node-port specified MUST be quoted, since ':' is not an allowed
  //   character in 'token'."
  @Nullable
  private static String extractIpAddress(String forwarded, int start) {
    if (forwarded.length() == start) {
      return null;
    }
    if (forwarded.charAt(start) == '"') {
      return extractIpAddress(forwarded, start + 1);
    }
    if (forwarded.charAt(start) == '[') {
      int end = forwarded.indexOf(']', start + 1);
      if (end == -1) {
        return null;
      }
      return forwarded.substring(start + 1, end);
    }
    boolean inIpv4 = false;
    for (int i = start; i < forwarded.length() - 1; i++) {
      char c = forwarded.charAt(i);
      if (c == '.') {
        inIpv4 = true;
      } else if (c == ',' || c == ';' || c == '"' || (inIpv4 && c == ':')) {
        if (i == start) { // empty string
          return null;
        }
        return forwarded.substring(start, i);
      }
    }
    return forwarded.substring(start);
  }

  private ForwarderHeaderParser() {}
}
