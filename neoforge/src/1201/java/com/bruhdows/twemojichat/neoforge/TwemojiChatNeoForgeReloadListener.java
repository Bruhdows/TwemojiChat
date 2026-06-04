package com.bruhdows.twemojichat.neoforge;

import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

public final class TwemojiChatNeoForgeReloadListener {
    private TwemojiChatNeoForgeReloadListener() {
    }

    public static void register(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new NeoForgeEmojiIndexReloader());
    }
}
