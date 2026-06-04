package com.bruhdows.twemojichat.version;

import com.bruhdows.twemojichat.TwemojiChat;
import com.bruhdows.twemojichat.client.emoji.EmojiIndex;
import java.io.IOException;
import java.io.Reader;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class VersionHooks12111 implements VersionHooks {
  private static final FontDescription EMOJI_FONT =
      new FontDescription.Resource(Identifier.fromNamespaceAndPath(TwemojiChat.MOD_ID, "emoji"));
  private static final Identifier INDEX_RESOURCE =
      Identifier.fromNamespaceAndPath(TwemojiChat.MOD_ID, "twemoji/index.json");

  @Override
  public void installInputFormatter(EditBox input, InputFormatter formatter) {
    input.addFormatter(formatter::format);
  }

  @Override
  public boolean isInputBordered(EditBox input) {
    return input.isBordered();
  }

  @Override
  public Style emojiStyle() {
    return Style.EMPTY.withFont(EMOJI_FONT);
  }

  @Override
  public EmojiIndex loadEmojiIndex(ResourceManager resourceManager) {
    try (Reader reader = resourceManager.openAsReader(INDEX_RESOURCE)) {
      return EmojiIndex.load(reader);
    } catch (IOException exception) {
      TwemojiChat.LOGGER.error("Failed to load Twemoji index {}", INDEX_RESOURCE, exception);
      return EmojiIndex.EMPTY;
    }
  }
}
