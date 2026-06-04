package com.bruhdows.twemojichat.version;

import net.minecraft.client.gui.components.EditBox;

public final class VersionHooksImpl implements VersionHooks {
  @Override
  public void installInputFormatter(EditBox input, InputFormatter formatter) {
    input.setFormatter(formatter::format);
  }

  @Override
  public boolean isInputBordered(EditBox input) {
    return input.isBordered();
  }
}
