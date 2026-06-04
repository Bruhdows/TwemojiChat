package com.bruhdows.twemojichat.platform;

import java.util.ServiceLoader;

public final class Services {
  private Services() {}

  public static <T> T load(Class<T> type) {
    return ServiceLoader.load(type)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException("Missing service implementation for " + type.getName()));
  }
}
