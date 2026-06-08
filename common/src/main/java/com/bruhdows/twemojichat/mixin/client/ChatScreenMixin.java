package com.bruhdows.twemojichat.mixin.client;

import com.bruhdows.twemojichat.client.TwemojiChatClientRuntime;
import com.bruhdows.twemojichat.client.emoji.EmojiTextRewriter;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
abstract class ChatScreenMixin {
  @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
  private void twemojichat$commitEmojiSuggestionOnEnter(
      int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
    if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
      return;
    }

    if (TwemojiChatClientRuntime.instance().tryCommitEmojiSuggestion(this)) {
      cir.setReturnValue(true);
    }
  }

  @Inject(method = "keyPressed", at = @At("RETURN"))
  private void twemojichat$refreshEmojiSuggestionsAfterKeyPress(
      int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
    if (cir.getReturnValue()) {
      TwemojiChatClientRuntime.instance().onChatInputChanged(this);
    }
  }

  @Inject(method = "normalizeChatMessage", at = @At("RETURN"), cancellable = true)
  private void twemojichat$normalizeEmojiGlyphs(
      String message, CallbackInfoReturnable<String> cir) {
    cir.setReturnValue(EmojiTextRewriter.normalizeOutgoing(cir.getReturnValue()));
  }
}
