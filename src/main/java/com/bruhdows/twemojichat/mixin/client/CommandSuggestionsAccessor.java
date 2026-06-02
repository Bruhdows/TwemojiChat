package com.bruhdows.twemojichat.mixin.client;

import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CommandSuggestions.class)
public interface CommandSuggestionsAccessor {
    @Accessor("font")
    Font twemojichat$getFont();

    @Accessor("fillColor")
    int twemojichat$getFillColor();

    @Accessor("suggestionLineLimit")
    int twemojichat$getSuggestionLineLimit();

    @Accessor("pendingSuggestions")
    void twemojichat$setPendingSuggestions(CompletableFuture<Suggestions> pendingSuggestions);

    @Accessor("suggestions")
    Object twemojichat$getSuggestions();

    @Invoker("showSuggestions")
    void twemojichat$invokeShowSuggestions(boolean narrateFirstSuggestion);

    @Invoker("hide")
    void twemojichat$invokeHide();
}
