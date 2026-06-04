package com.bruhdows.twemojichat.platform;

import java.nio.file.Path;

public interface PlatformHelper {
  PlatformHelper INSTANCE = Services.load(PlatformHelper.class);

  Path getConfigDirectory();

  boolean isDevelopmentEnvironment();

  LoaderKind getLoaderKind();
}
