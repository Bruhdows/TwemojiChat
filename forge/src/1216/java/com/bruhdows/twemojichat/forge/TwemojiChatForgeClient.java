package com.bruhdows.twemojichat.forge;

import com.bruhdows.twemojichat.client.TwemojiChatClientEntrypoint;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class TwemojiChatForgeClient {
  private static final TwemojiChatClientEntrypoint ENTRYPOINT = new TwemojiChatClientEntrypoint();

  private TwemojiChatForgeClient() {}

  public static void init(FMLJavaModLoadingContext context) {
    ENTRYPOINT.initialize();
    RegisterClientReloadListenersEvent.getBus(context.getModBusGroup())
        .addListener(
            (RegisterClientReloadListenersEvent event) ->
                TwemojiChatForgeReloadListener.register(event, ENTRYPOINT));
    var events = new TwemojiChatForgeEvents(ENTRYPOINT);
    ClientChatReceivedEvent.BUS.addListener(events::onChatReceived);
    ScreenEvent.Init.Post.BUS.addListener(events::onScreenInit);
    ScreenEvent.Closing.BUS.addListener(events::onScreenClosing);
    ScreenEvent.Render.Post.BUS.addListener(events::onScreenRender);
  }
}
