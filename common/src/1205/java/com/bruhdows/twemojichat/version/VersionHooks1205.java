package com.bruhdows.twemojichat.version;

import com.bruhdows.twemojichat.TwemojiChat;
import com.bruhdows.twemojichat.client.emoji.EmojiIndex;
import java.io.IOException;
import java.io.Reader;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public final class VersionHooks1205 implements VersionHooks {
  @SuppressWarnings("removal")
  private static final ResourceLocation EMOJI_FONT =
      new ResourceLocation(TwemojiChat.MOD_ID, "emoji");

  @SuppressWarnings("removal")
  private static final ResourceLocation INDEX_RESOURCE =
      new ResourceLocation(TwemojiChat.MOD_ID, "twemoji/index.json");

  @Override
  public void installInputFormatter(EditBox input, InputFormatter formatter) {
    input.setFormatter(formatter::format);
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
