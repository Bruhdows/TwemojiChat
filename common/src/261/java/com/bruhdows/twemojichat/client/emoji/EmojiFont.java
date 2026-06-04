package com.bruhdows.twemojichat.client.emoji;

import com.bruhdows.twemojichat.TwemojiChat;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class EmojiFont {
  public static final FontDescription ID =
      new FontDescription.Resource(Identifier.fromNamespaceAndPath(TwemojiChat.MOD_ID, "emoji"));

  private EmojiFont() {}

  public static Style style() {
    return Style.EMPTY.withFont(ID);
  }
}
