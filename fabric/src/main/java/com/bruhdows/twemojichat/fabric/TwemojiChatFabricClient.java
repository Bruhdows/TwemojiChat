package com.bruhdows.twemojichat.fabric;

import com.bruhdows.twemojichat.client.TwemojiChatClientRuntime;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;

public final class TwemojiChatFabricClient implements ClientModInitializer {
    private static final TwemojiChatClientRuntime RUNTIME = new TwemojiChatClientRuntime();

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new TwemojiChatFabricReloadListener());
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> RUNTIME.rewriteReceivedMessage(message));
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            Minecraft.getInstance().gui.getChat().addMessage(RUNTIME.rewriteReceivedMessage(message));
            return false;
        });
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            RUNTIME.onScreenInit(screen);
            ScreenEvents.afterRender(screen).register((currentScreen, drawContext, mouseX, mouseY, tickDelta) -> RUNTIME.onScreenRender(currentScreen));
            ScreenEvents.remove(screen).register(RUNTIME::onScreenClosing);
        });
    }
}
