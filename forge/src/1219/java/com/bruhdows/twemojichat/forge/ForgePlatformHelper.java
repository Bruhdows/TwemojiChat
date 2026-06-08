package com.bruhdows.twemojichat.forge;

import com.bruhdows.twemojichat.platform.LoaderKind;
import com.bruhdows.twemojichat.platform.PlatformHelper;
import java.nio.file.Path;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

public final class ForgePlatformHelper implements PlatformHelper {
  @Override
  public Path getConfigDirectory() {
    return FMLPaths.CONFIGDIR.get();
  }

  @Override
  public boolean isDevelopmentEnvironment() {
    return !FMLLoader.isProduction();
  }

  @Override
  public LoaderKind getLoaderKind() {
    return LoaderKind.FORGE;
  }
}
