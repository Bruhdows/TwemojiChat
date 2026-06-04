package com.bruhdows.twemojichat.fabric;

import com.bruhdows.twemojichat.TwemojiChat;
import com.bruhdows.twemojichat.client.emoji.EmojiIndex;
import com.bruhdows.twemojichat.client.emoji.EmojiIndexReloader;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class TwemojiChatFabricReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath(TwemojiChat.MOD_ID, "emoji_index");
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        EmojiIndex prepared = EmojiIndexReloader.load(resourceManager);
        EmojiIndexReloader.apply(prepared);
    }
}
