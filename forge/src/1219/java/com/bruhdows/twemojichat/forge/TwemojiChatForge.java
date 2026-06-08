package com.bruhdows.twemojichat.forge;

import com.bruhdows.twemojichat.TwemojiChat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(TwemojiChat.MOD_ID)
public final class TwemojiChatForge {
  public TwemojiChatForge(FMLJavaModLoadingContext context) {
    if (FMLEnvironment.dist == Dist.CLIENT) {
      TwemojiChatForgeClient.init(context);
    }
  }
}
