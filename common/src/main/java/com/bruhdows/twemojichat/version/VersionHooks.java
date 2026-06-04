package com.bruhdows.twemojichat.version;

import com.bruhdows.twemojichat.client.emoji.EmojiIndex;
import com.bruhdows.twemojichat.platform.Services;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.resources.ResourceManager;

public interface VersionHooks {
  VersionHooks INSTANCE = Services.load(VersionHooks.class);

  void installInputFormatter(EditBox input, InputFormatter formatter);

  boolean isInputBordered(EditBox input);

  Style emojiStyle();

  EmojiIndex loadEmojiIndex(ResourceManager resourceManager);
}
