package com.bruhdows.twemojichat.fabric;

import com.bruhdows.twemojichat.client.TwemojiChatClientEntrypoint;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;

public final class TwemojiChatFabricClient implements ClientModInitializer {
  private static final TwemojiChatClientEntrypoint ENTRYPOINT = new TwemojiChatClientEntrypoint();

  @Override
  public void onInitializeClient() {
    ENTRYPOINT.initialize();
    ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
        .registerReloadListener(new TwemojiChatFabricReloadListener(ENTRYPOINT));
    ClientReceiveMessageEvents.MODIFY_GAME.register(
        (message, overlay) -> ENTRYPOINT.onChatMessageReceived(message));
    ClientReceiveMessageEvents.ALLOW_CHAT.register(
        (message, signedMessage, sender, params, receptionTimestamp) -> {
          Minecraft.getInstance()
              .gui
              .getChat()
              .addPlayerMessage(
                  ENTRYPOINT.onChatMessageReceived(message), signedMessage.signature(), null);
          return false;
        });
    ScreenEvents.AFTER_INIT.register(
        (client, screen, scaledWidth, scaledHeight) -> {
          ENTRYPOINT.onScreenInit(screen);
          ScreenEvents.afterExtract(screen)
              .register(
                  (currentScreen, guiGraphicsExtractor, mouseX, mouseY, tickDelta) ->
                      ENTRYPOINT.onScreenRender(currentScreen));
          ScreenEvents.remove(screen).register(ENTRYPOINT::onScreenClosing);
        });
  }
}
