package com.bruhdows.twemojichat.neoforge;

import com.bruhdows.twemojichat.client.emoji.EmojiIndexReloader;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

public final class TwemojiChatNeoForgeReloadListener {
    private TwemojiChatNeoForgeReloadListener() {
    }

    public static void register(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new NeoForgeEmojiIndexReloader());
    }
}
