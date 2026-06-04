package com.bruhdows.twemojichat.fabric;

import com.bruhdows.twemojichat.TwemojiChat;
import com.bruhdows.twemojichat.client.TwemojiChatClientEntrypoint;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public final class TwemojiChatFabricReloadListener
    implements SimpleSynchronousResourceReloadListener {
  private final TwemojiChatClientEntrypoint entrypoint;

  public TwemojiChatFabricReloadListener(TwemojiChatClientEntrypoint entrypoint) {
    this.entrypoint = entrypoint;
  }

  @Override
  public ResourceLocation getFabricId() {
    return ResourceLocation.fromNamespaceAndPath(TwemojiChat.MOD_ID, "emoji_index");
  }

  @Override
  public void onResourceManagerReload(ResourceManager resourceManager) {
    this.entrypoint.onEmojiIndexReload(resourceManager);
  }
}
