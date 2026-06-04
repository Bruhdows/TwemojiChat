package com.bruhdows.twemojichat.neoforge;

import com.bruhdows.twemojichat.client.TwemojiChatClientEntrypoint;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public final class TwemojiChatNeoForgeClient {
  private static final TwemojiChatClientEntrypoint ENTRYPOINT = new TwemojiChatClientEntrypoint();

  private TwemojiChatNeoForgeClient() {}

  public static void init(IEventBus modEventBus) {
    ENTRYPOINT.initialize();
    modEventBus.addListener(
        (RegisterClientReloadListenersEvent event) ->
            TwemojiChatNeoForgeReloadListener.register(event, ENTRYPOINT));
    MinecraftForge.EVENT_BUS.register(new TwemojiChatNeoForgeEvents(ENTRYPOINT));
  }
}
