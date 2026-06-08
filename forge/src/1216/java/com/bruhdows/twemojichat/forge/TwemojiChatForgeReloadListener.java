package com.bruhdows.twemojichat.forge;

import com.bruhdows.twemojichat.client.TwemojiChatClientEntrypoint;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

public final class TwemojiChatForgeReloadListener {
  private TwemojiChatForgeReloadListener() {}

  public static void register(
      RegisterClientReloadListenersEvent event, TwemojiChatClientEntrypoint entrypoint) {
    event.registerReloadListener(new ForgeEmojiIndexReloader(entrypoint));
  }
}
