package com.bruhdows.twemojichat.client;

import com.bruhdows.twemojichat.TwemojiChat;
import com.bruhdows.twemojichat.client.emoji.EmojiIndexReloader;
import com.bruhdows.twemojichat.platform.PlatformHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;

public final class TwemojiChatClientEntrypoint {
  private final TwemojiChatClientRuntime runtime = TwemojiChatClientRuntime.instance();
  private boolean initialized;

  public void initialize() {
    if (this.initialized) {
      return;
    }

    PlatformHelper platform = PlatformHelper.INSTANCE;
    TwemojiChat.LOGGER.info(
        "Initializing {} on {} (config={})",
        TwemojiChat.MOD_ID,
        platform.getLoaderKind(),
        platform.getConfigDirectory());
    this.initialized = true;
  }

  public Component onChatMessageReceived(Component message) {
    return this.runtime.rewriteReceivedMessage(message);
  }

  public void onScreenInit(Object screen) {
    this.runtime.onScreenInit(screen);
  }

  public void onScreenClosing(Object screen) {
    this.runtime.onScreenClosing(screen);
  }

  public void onScreenRender(Object screen) {
    this.runtime.onScreenRender(screen);
  }

  public void onEmojiIndexReload(ResourceManager resourceManager) {
    EmojiIndexReloader.apply(EmojiIndexReloader.load(resourceManager));
  }
}
