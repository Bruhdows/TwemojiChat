package com.bruhdows.twemojichat.client;

import com.bruhdows.twemojichat.client.chat.ChatEmojiController;
import com.bruhdows.twemojichat.client.emoji.EmojiTextRewriter;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;

public final class TwemojiChatClientRuntime {
  private static final TwemojiChatClientRuntime INSTANCE = new TwemojiChatClientRuntime();

  private final Map<ChatScreen, ChatEmojiController> controllers = new WeakHashMap<>();

  public static TwemojiChatClientRuntime instance() {
    return INSTANCE;
  }

  public Component rewriteReceivedMessage(Component message) {
    return EmojiTextRewriter.rewrite(message);
  }

  public void onScreenInit(Object screen) {
    if (screen instanceof ChatScreen chatScreen) {
      this.controllers.put(chatScreen, new ChatEmojiController(chatScreen));
    }
  }

  public void onScreenClosing(Object screen) {
    if (screen instanceof ChatScreen chatScreen) {
      this.controllers.remove(chatScreen);
    }
  }

  public void onScreenRender(Object screen) {
    if (screen instanceof ChatScreen chatScreen) {
      this.controller(chatScreen).refresh();
    }
  }

  public void onChatInputChanged(Object screen) {
    if (screen instanceof ChatScreen chatScreen) {
      this.controller(chatScreen).refresh();
    }
  }

  public boolean tryCommitEmojiSuggestion(Object screen) {
    if (screen instanceof ChatScreen chatScreen) {
      return this.controller(chatScreen).tryCommitCurrentSuggestion();
    }

    return false;
  }

  private ChatEmojiController controller(ChatScreen chatScreen) {
    return this.controllers.computeIfAbsent(chatScreen, ChatEmojiController::new);
  }
}
