package com.bruhdows.twemojichat.neoforge;

import com.bruhdows.twemojichat.client.TwemojiChatClientRuntime;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class TwemojiChatNeoForgeEvents {
    private final TwemojiChatClientRuntime runtime = new TwemojiChatClientRuntime();

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        event.setMessage(this.runtime.rewriteReceivedMessage(event.getMessage()));
    }

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof ChatScreen chatScreen) {
            this.runtime.onScreenInit(chatScreen);
        }
    }

    @SubscribeEvent
    public void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof ChatScreen chatScreen) {
            this.runtime.onScreenClosing(chatScreen);
        }
    }

    @SubscribeEvent
    public void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof ChatScreen chatScreen) {
            this.runtime.onScreenRender(chatScreen);
        }
    }
}
