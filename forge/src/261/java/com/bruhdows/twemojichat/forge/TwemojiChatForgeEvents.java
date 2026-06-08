package com.bruhdows.twemojichat.forge;

import com.bruhdows.twemojichat.client.TwemojiChatClientEntrypoint;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ScreenEvent;

public final class TwemojiChatForgeEvents {
  private final TwemojiChatClientEntrypoint entrypoint;

  public TwemojiChatForgeEvents(TwemojiChatClientEntrypoint entrypoint) {
    this.entrypoint = entrypoint;
  }

  public void onChatReceived(ClientChatReceivedEvent event) {
    event.setMessage(this.entrypoint.onChatMessageReceived(event.getMessage()));
  }

  public void onScreenInit(ScreenEvent.Init.Post event) {
    if (event.getScreen() instanceof ChatScreen chatScreen) {
      this.entrypoint.onScreenInit(chatScreen);
    }
  }

  public void onScreenClosing(ScreenEvent.Closing event) {
    if (event.getScreen() instanceof ChatScreen chatScreen) {
      this.entrypoint.onScreenClosing(chatScreen);
    }
  }

  public void onScreenRender(ScreenEvent.Render.Post event) {
    if (event.getScreen() instanceof ChatScreen chatScreen) {
      this.entrypoint.onScreenRender(chatScreen);
    }
  }
}
