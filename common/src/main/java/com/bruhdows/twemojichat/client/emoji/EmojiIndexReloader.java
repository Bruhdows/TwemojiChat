package com.bruhdows.twemojichat.client.emoji;

import com.bruhdows.twemojichat.TwemojiChat;
import java.io.IOException;
import java.io.Reader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public final class EmojiIndexReloader {
  private static final ResourceLocation INDEX_RESOURCE =
      ResourceLocation.fromNamespaceAndPath(TwemojiChat.MOD_ID, "twemoji/index.json");
  private static volatile EmojiIndex index = EmojiIndex.EMPTY;

  private EmojiIndexReloader() {}

  public static EmojiIndex getIndex() {
    return index;
  }

  public static EmojiIndex load(ResourceManager resourceManager) {
    try (Reader reader = resourceManager.openAsReader(INDEX_RESOURCE)) {
      return EmojiIndex.load(reader);
    } catch (IOException exception) {
      TwemojiChat.LOGGER.error("Failed to load Twemoji index {}", INDEX_RESOURCE, exception);
      return EmojiIndex.EMPTY;
    }
  }

  public static void apply(EmojiIndex prepared) {
    index = prepared;
    TwemojiChat.LOGGER.info("Loaded {} Twemoji definitions", prepared.size());
  }
}
