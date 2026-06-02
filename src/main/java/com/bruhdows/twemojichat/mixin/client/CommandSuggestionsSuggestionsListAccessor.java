package com.bruhdows.twemojichat.mixin.client;

import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSuggestions.SuggestionsList.class)
public interface CommandSuggestionsSuggestionsListAccessor {
    @Accessor("tabCycles")
    void twemojichat$setTabCycles(boolean tabCycles);
}
