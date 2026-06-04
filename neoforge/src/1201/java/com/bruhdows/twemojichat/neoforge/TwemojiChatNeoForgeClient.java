package com.bruhdows.twemojichat.neoforge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public final class TwemojiChatNeoForgeClient {
    private TwemojiChatNeoForgeClient() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(TwemojiChatNeoForgeReloadListener::register);
        MinecraftForge.EVENT_BUS.register(new TwemojiChatNeoForgeEvents());
    }
}
