package com.bruhdows.twemojichat.neoforge;

import com.bruhdows.twemojichat.platform.LoaderKind;
import com.bruhdows.twemojichat.platform.PlatformHelper;
import java.nio.file.Path;
import net.neoforged.fml.loading.FMLPaths;

public final class NeoForgePlatformHelper implements PlatformHelper {
  @Override
  public Path getConfigDirectory() {
    return FMLPaths.CONFIGDIR.get();
  }

  @Override
  public boolean isDevelopmentEnvironment() {
    return !Boolean.getBoolean("production");
  }

  @Override
  public LoaderKind getLoaderKind() {
    return LoaderKind.NEOFORGE;
  }
}
