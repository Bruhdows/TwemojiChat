package com.bruhdows.twemojichat.client.emoji;

import com.bruhdows.twemojichat.TwemojiChat;
import com.bruhdows.twemojichat.version.VersionHooks;
import net.minecraft.server.packs.resources.ResourceManager;

public final class EmojiIndexReloader {
  private static volatile EmojiIndex index = EmojiIndex.EMPTY;

  private EmojiIndexReloader() {}

  public static EmojiIndex getIndex() {
    return index;
  }

  public static EmojiIndex load(ResourceManager resourceManager) {
    return VersionHooks.INSTANCE.loadEmojiIndex(resourceManager);
  }

  public static void apply(EmojiIndex prepared) {
    index = prepared;
    TwemojiChat.LOGGER.info("Loaded {} Twemoji definitions", prepared.size());
  }
}
