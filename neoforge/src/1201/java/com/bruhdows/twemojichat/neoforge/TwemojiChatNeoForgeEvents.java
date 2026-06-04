package com.bruhdows.twemojichat.neoforge;

import com.bruhdows.twemojichat.client.TwemojiChatClientRuntime;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
