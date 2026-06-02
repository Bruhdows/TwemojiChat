package com.bruhdows.twemojichat;

import com.bruhdows.twemojichat.client.TwemojiChatEvents;
import com.bruhdows.twemojichat.client.emoji.EmojiIndexReloader;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public final class TwemojiChatClient {
    private TwemojiChatClient() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(EmojiIndexReloader::register);
        NeoForge.EVENT_BUS.register(new TwemojiChatEvents());
    }
}
