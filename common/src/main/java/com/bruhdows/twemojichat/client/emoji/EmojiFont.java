package com.bruhdows.twemojichat.client.emoji;

import com.bruhdows.twemojichat.version.VersionHooks;

public final class EmojiFont {
  private EmojiFont() {}

  public static net.minecraft.network.chat.Style style() {
    return VersionHooks.INSTANCE.emojiStyle();
  }
}
