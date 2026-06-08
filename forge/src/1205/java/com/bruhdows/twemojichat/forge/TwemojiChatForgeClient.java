package com.bruhdows.twemojichat.forge;

import com.bruhdows.twemojichat.client.TwemojiChatClientEntrypoint;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public final class TwemojiChatForgeClient {
  private static final TwemojiChatClientEntrypoint ENTRYPOINT = new TwemojiChatClientEntrypoint();

  private TwemojiChatForgeClient() {}

  public static void init(IEventBus modEventBus) {
    ENTRYPOINT.initialize();
    modEventBus.addListener(
        (RegisterClientReloadListenersEvent event) ->
            TwemojiChatForgeReloadListener.register(event, ENTRYPOINT));
    MinecraftForge.EVENT_BUS.register(new TwemojiChatForgeEvents(ENTRYPOINT));
  }
}
