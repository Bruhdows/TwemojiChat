package com.bruhdows.twemojichat.mixin.client;

import com.bruhdows.twemojichat.client.emoji.EmojiTextRewriter;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
abstract class ChatScreenMixin {
  @Inject(method = "normalizeChatMessage", at = @At("RETURN"), cancellable = true)
  private void twemojichat$normalizeEmojiGlyphs(
      String message, CallbackInfoReturnable<String> cir) {
    cir.setReturnValue(EmojiTextRewriter.normalizeOutgoing(cir.getReturnValue()));
  }
}
