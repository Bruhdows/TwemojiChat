package com.bruhdows.twemojichat.neoforge;

import com.bruhdows.twemojichat.TwemojiChat;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.minecraft.resources.Identifier;

public final class TwemojiChatNeoForgeReloadListener {
    private TwemojiChatNeoForgeReloadListener() {
    }

    public static void register(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(TwemojiChat.MOD_ID, "emoji_index"), new NeoForgeEmojiIndexReloader());
    }
}
