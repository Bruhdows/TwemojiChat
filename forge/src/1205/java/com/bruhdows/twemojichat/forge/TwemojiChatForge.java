package com.bruhdows.twemojichat.forge;

import com.bruhdows.twemojichat.TwemojiChat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(TwemojiChat.MOD_ID)
public final class TwemojiChatForge {
  public TwemojiChatForge(IEventBus modEventBus) {
    if (FMLEnvironment.dist == Dist.CLIENT) {
      TwemojiChatForgeClient.init(modEventBus);
    }
  }
}
