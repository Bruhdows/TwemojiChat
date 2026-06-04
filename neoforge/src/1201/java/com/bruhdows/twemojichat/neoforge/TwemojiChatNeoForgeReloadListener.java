package com.bruhdows.twemojichat.neoforge;

import com.bruhdows.twemojichat.client.TwemojiChatClientEntrypoint;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

public final class TwemojiChatNeoForgeReloadListener {
  private TwemojiChatNeoForgeReloadListener() {}

  public static void register(
      RegisterClientReloadListenersEvent event, TwemojiChatClientEntrypoint entrypoint) {
    event.registerReloadListener(new NeoForgeEmojiIndexReloader(entrypoint));
  }
}
