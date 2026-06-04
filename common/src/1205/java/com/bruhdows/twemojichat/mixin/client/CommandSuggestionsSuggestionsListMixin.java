package com.bruhdows.twemojichat.mixin.client;

import com.bruhdows.twemojichat.client.emoji.EmojiDefinition;
import com.bruhdows.twemojichat.client.emoji.EmojiFont;
import com.bruhdows.twemojichat.client.emoji.EmojiIndexReloader;
import com.mojang.brigadier.suggestion.Suggestion;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.CommandSuggestions.SuggestionsList;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SuggestionsList.class)
abstract class CommandSuggestionsSuggestionsListMixin {
  @Shadow @Final private Rect2i rect;

  @Shadow @Final private List<Suggestion> suggestionList;

  @Shadow private int offset;

  @Shadow private int current;

  @Shadow private Vec2 lastMouse;

  @Shadow
  public abstract void select(int index);

  @Shadow
  public abstract void useSuggestion();

  @Shadow @Final CommandSuggestions field_21615;

  @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
  private void twemojichat$commitEmojiSuggestionOnTab(
      int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
    if (!this.twemojichat$hasEmojiSuggestions() || keyCode != GLFW.GLFW_KEY_TAB) {
      return;
    }

    this.useSuggestion();
    cir.setReturnValue(true);
  }

  @Inject(method = "render", at = @At("HEAD"), cancellable = true)
  private void twemojichat$renderEmojiFirst(
      GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
    if (!this.twemojichat$hasEmojiSuggestions()) {
      return;
    }

    ci.cancel();
    CommandSuggestionsAccessor accessor = (CommandSuggestionsAccessor) this.field_21615;
    int visible =
        Math.min(this.suggestionList.size(), accessor.twemojichat$getSuggestionLineLimit());
    int defaultTextColor = -5592406;
    boolean hasTopOverflow = this.offset > 0;
    boolean hasBottomOverflow = this.suggestionList.size() > this.offset + visible;
    boolean hasOverflow = hasTopOverflow || hasBottomOverflow;
    boolean mouseMoved = this.lastMouse.x != (float) mouseX || this.lastMouse.y != (float) mouseY;
    if (mouseMoved) {
      this.lastMouse = new Vec2((float) mouseX, (float) mouseY);
    }

    if (hasOverflow) {
      guiGraphics.fill(
          this.rect.getX(),
          this.rect.getY() - 1,
          this.rect.getX() + this.rect.getWidth(),
          this.rect.getY(),
          accessor.twemojichat$getFillColor());
      guiGraphics.fill(
          this.rect.getX(),
          this.rect.getY() + this.rect.getHeight(),
          this.rect.getX() + this.rect.getWidth(),
          this.rect.getY() + this.rect.getHeight() + 1,
          accessor.twemojichat$getFillColor());
      if (hasTopOverflow) {
        for (int index = 0; index < this.rect.getWidth(); index++) {
          if (index % 2 == 0) {
            guiGraphics.fill(
                this.rect.getX() + index,
                this.rect.getY() - 1,
                this.rect.getX() + index + 1,
                this.rect.getY(),
                -1);
          }
        }
      }

      if (hasBottomOverflow) {
        for (int index = 0; index < this.rect.getWidth(); index++) {
          if (index % 2 == 0) {
            guiGraphics.fill(
                this.rect.getX() + index,
                this.rect.getY() + this.rect.getHeight(),
                this.rect.getX() + index + 1,
                this.rect.getY() + this.rect.getHeight() + 1,
                -1);
          }
        }
      }
    }

    boolean hovered = false;
    Font font = accessor.twemojichat$getFont();

    for (int row = 0; row < visible; row++) {
      Suggestion suggestion = this.suggestionList.get(row + this.offset);
      guiGraphics.fill(
          this.rect.getX(),
          this.rect.getY() + 12 * row,
          this.rect.getX() + this.rect.getWidth(),
          this.rect.getY() + 12 * row + 12,
          accessor.twemojichat$getFillColor());
      if (mouseX > this.rect.getX()
          && mouseX < this.rect.getX() + this.rect.getWidth()
          && mouseY > this.rect.getY() + 12 * row
          && mouseY < this.rect.getY() + 12 * row + 12) {
        if (mouseMoved) {
          this.select(row + this.offset);
        }
        hovered = true;
      }

      Component display = this.twemojichat$displayComponent(suggestion);
      guiGraphics.drawString(
          font,
          display,
          this.rect.getX() + 1,
          this.rect.getY() + 2 + 12 * row,
          row + this.offset == this.current ? -256 : defaultTextColor);
    }
  }

  @Unique
  private boolean twemojichat$hasEmojiSuggestions() {
    return !this.suggestionList.isEmpty()
        && this.suggestionList.stream().allMatch(this::twemojichat$isEmojiSuggestion);
  }

  @Unique
  private boolean twemojichat$isEmojiSuggestion(Suggestion suggestion) {
    return suggestion.getText().startsWith(":") && suggestion.getText().endsWith(":");
  }

  @Unique
  private Component twemojichat$displayComponent(Suggestion suggestion) {
    EmojiDefinition definition = this.twemojichat$definitionForSuggestion(suggestion);
    if (definition == null) {
      return Component.literal(suggestion.getText());
    }

    return Component.empty()
        .append(Component.literal(definition.glyph()).withStyle(EmojiFont.style()))
        .append(Component.literal(" " + suggestion.getText()));
  }

  @Unique
  private EmojiDefinition twemojichat$definitionForSuggestion(Suggestion suggestion) {
    String text = suggestion.getText();
    if (text.length() < 2 || text.charAt(0) != ':' || text.charAt(text.length() - 1) != ':') {
      return null;
    }

    return EmojiIndexReloader.getIndex()
        .byAlias(text.substring(1, text.length() - 1).toLowerCase(Locale.ROOT));
  }
}
