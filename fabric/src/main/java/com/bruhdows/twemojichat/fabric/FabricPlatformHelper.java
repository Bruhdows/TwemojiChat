package com.bruhdows.twemojichat.fabric;

import com.bruhdows.twemojichat.platform.LoaderKind;
import com.bruhdows.twemojichat.platform.PlatformHelper;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricPlatformHelper implements PlatformHelper {
  @Override
  public Path getConfigDirectory() {
    return FabricLoader.getInstance().getConfigDir();
  }

  @Override
  public boolean isDevelopmentEnvironment() {
    return FabricLoader.getInstance().isDevelopmentEnvironment();
  }

  @Override
  public LoaderKind getLoaderKind() {
    return LoaderKind.FABRIC;
  }
}
