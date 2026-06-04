package com.bruhdows.twemojichat.version;

import com.bruhdows.twemojichat.mixin.client.EditBoxAccessor;
import net.minecraft.client.gui.components.EditBox;

public final class VersionHooksImpl implements VersionHooks {
  @Override
  public void installInputFormatter(EditBox input, InputFormatter formatter) {
    input.setFormatter(formatter::format);
  }

  @Override
  public boolean isInputBordered(EditBox input) {
    return ((EditBoxAccessor) input).twemojichat$isBordered();
  }
}
