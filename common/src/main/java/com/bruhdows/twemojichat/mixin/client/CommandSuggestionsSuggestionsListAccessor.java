package com.bruhdows.twemojichat.mixin.client;

import com.mojang.brigadier.suggestion.Suggestion;
import java.util.List;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSuggestions.SuggestionsList.class)
public interface CommandSuggestionsSuggestionsListAccessor {
  @Accessor("rect")
  Rect2i twemojichat$getRect();

  @Accessor("suggestionList")
  List<Suggestion> twemojichat$getSuggestionList();

  @Accessor("current")
  int twemojichat$getCurrent();
}
