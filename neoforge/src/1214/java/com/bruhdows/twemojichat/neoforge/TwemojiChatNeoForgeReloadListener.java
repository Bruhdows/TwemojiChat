package com.bruhdows.twemojichat.neoforge;

import com.bruhdows.twemojichat.TwemojiChat;
import com.bruhdows.twemojichat.client.TwemojiChatClientEntrypoint;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

public final class TwemojiChatNeoForgeReloadListener {
  private TwemojiChatNeoForgeReloadListener() {}

  public static void register(
      AddClientReloadListenersEvent event, TwemojiChatClientEntrypoint entrypoint) {
    event.addListener(
        ResourceLocation.fromNamespaceAndPath(TwemojiChat.MOD_ID, "emoji_index"),
        new NeoForgeEmojiIndexReloader(entrypoint));
  }
}
