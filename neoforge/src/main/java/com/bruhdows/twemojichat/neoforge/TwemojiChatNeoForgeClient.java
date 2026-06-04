package com.bruhdows.twemojichat.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public final class TwemojiChatNeoForgeClient {
    private TwemojiChatNeoForgeClient() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(TwemojiChatNeoForgeReloadListener::register);
        NeoForge.EVENT_BUS.register(new TwemojiChatNeoForgeEvents());
    }
}
