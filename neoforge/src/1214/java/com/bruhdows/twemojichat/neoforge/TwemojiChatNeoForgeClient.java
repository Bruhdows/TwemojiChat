package com.bruhdows.twemojichat.neoforge;

import com.bruhdows.twemojichat.client.TwemojiChatClientEntrypoint;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class TwemojiChatNeoForgeClient {
  private static final TwemojiChatClientEntrypoint ENTRYPOINT = new TwemojiChatClientEntrypoint();

  private TwemojiChatNeoForgeClient() {}

  public static void init(IEventBus modEventBus) {
    ENTRYPOINT.initialize();
    modEventBus.addListener(
        (AddClientReloadListenersEvent event) ->
            TwemojiChatNeoForgeReloadListener.register(event, ENTRYPOINT));
    NeoForge.EVENT_BUS.register(new TwemojiChatNeoForgeEvents(ENTRYPOINT));
  }
}
