package com.bruhdows.twemojichat.client;

import com.bruhdows.twemojichat.client.chat.ChatEmojiController;
import com.bruhdows.twemojichat.client.emoji.EmojiTextRewriter;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class TwemojiChatEvents {
    private final Map<ChatScreen, ChatEmojiController> controllers = new WeakHashMap<>();

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        event.setMessage(EmojiTextRewriter.rewrite(event.getMessage()));
    }

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof ChatScreen chatScreen) {
            this.controllers.put(chatScreen, new ChatEmojiController(chatScreen));
        }
    }

    @SubscribeEvent
    public void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof ChatScreen chatScreen) {
            this.controllers.remove(chatScreen);
        }
    }

    @SubscribeEvent
    public void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof ChatScreen chatScreen) {
            this.controller(chatScreen).refresh();
        }
    }

    private ChatEmojiController controller(ChatScreen chatScreen) {
        return this.controllers.computeIfAbsent(chatScreen, ChatEmojiController::new);
    }
}
