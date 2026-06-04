package com.bruhdows.twemojichat.neoforge;

import com.bruhdows.twemojichat.TwemojiChat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = TwemojiChat.MOD_ID, dist = Dist.CLIENT)
public final class TwemojiChatNeoForge {
  public TwemojiChatNeoForge(IEventBus modEventBus) {
    TwemojiChatNeoForgeClient.init(modEventBus);
  }
}
