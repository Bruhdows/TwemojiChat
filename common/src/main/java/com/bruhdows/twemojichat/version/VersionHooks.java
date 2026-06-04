package com.bruhdows.twemojichat.version;

import com.bruhdows.twemojichat.platform.Services;
import net.minecraft.client.gui.components.EditBox;

public interface VersionHooks {
  VersionHooks INSTANCE = Services.load(VersionHooks.class);

  void installInputFormatter(EditBox input, InputFormatter formatter);

  boolean isInputBordered(EditBox input);
}
