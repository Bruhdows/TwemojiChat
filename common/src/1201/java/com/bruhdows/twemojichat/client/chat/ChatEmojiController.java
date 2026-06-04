package com.bruhdows.twemojichat.client.chat;

import com.bruhdows.twemojichat.client.emoji.EmojiDefinition;
import com.bruhdows.twemojichat.client.emoji.EmojiFont;
import com.bruhdows.twemojichat.client.emoji.EmojiIndex;
import com.bruhdows.twemojichat.client.emoji.EmojiIndexReloader;
import com.bruhdows.twemojichat.client.emoji.EmojiTextRewriter;
import com.bruhdows.twemojichat.mixin.client.ChatScreenAccessor;
import com.bruhdows.twemojichat.mixin.client.CommandSuggestionsAccessor;
import com.bruhdows.twemojichat.mixin.client.CommandSuggestionsSuggestionsListAccessor;
import com.bruhdows.twemojichat.mixin.client.EditBoxAccessor;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class ChatEmojiController {
    private final ChatScreen screen;
    private ActiveToken token;
    private String lastValue = "";
    private int lastCursor = -1;
    private boolean emojiSuggestionsVisible;
    private EmojiIndex cachedPopupWidthIndex = EmojiIndex.EMPTY;
    private int cachedPopupWidth = -1;

    public ChatEmojiController(ChatScreen screen) {
        this.screen = screen;
        this.installFormatter();
        this.primePopupWidth();
    }

    public void refresh() {
        EditBox input = this.input();
        String value = input.getValue();
        int cursor = input.getCursorPosition();
        if (value.equals(this.lastValue) && cursor == this.lastCursor) {
            return;
        }

        this.lastValue = value;
        this.lastCursor = cursor;

        CommandSuggestions commandSuggestions = this.commandSuggestions();
        if (value.startsWith("/")) {
            this.token = null;
            this.emojiSuggestionsVisible = false;
            return;
        }

        EmojiIndex index = EmojiIndexReloader.getIndex();
        if (this.replaceCompletedShortcode(value, cursor, index)) {
            return;
        }

        this.token = ActiveToken.find(value, cursor);
        if (this.token == null) {
            this.hideEmojiSuggestions(commandSuggestions);
            return;
        }

        List<EmojiDefinition> matches = index.complete(this.token.query(), index.size());
        if (matches.isEmpty()) {
            this.hideEmojiSuggestions(commandSuggestions);
            this.token = null;
            return;
        }

        List<Suggestion> suggestionsList = matches.stream().map(this::toSuggestion).toList();
        Suggestions suggestions = new Suggestions(
            StringRange.between(this.token.start(), this.token.end()),
            suggestionsList
        );
        CommandSuggestionsAccessor accessor = (CommandSuggestionsAccessor)commandSuggestions;
        accessor.twemojichat$setPendingSuggestions(CompletableFuture.completedFuture(suggestions));
        accessor.twemojichat$invokeShowSuggestions(false);
        this.emojiSuggestionsVisible = true;
        this.resizePopup(accessor, suggestionsList, index);
    }

    private Suggestion toSuggestion(EmojiDefinition definition) {
        String replacement = ":" + definition.primaryAlias() + ":";
        return new Suggestion(StringRange.between(this.token.start(), this.token.end()), replacement);
    }

    private void hideEmojiSuggestions(CommandSuggestions commandSuggestions) {
        if (!this.emojiSuggestionsVisible) {
            return;
        }

        CommandSuggestionsAccessor accessor = (CommandSuggestionsAccessor)commandSuggestions;
        accessor.twemojichat$setPendingSuggestions(Suggestions.empty());
        accessor.twemojichat$invokeHide();
        this.input().setSuggestion(null);
        this.emojiSuggestionsVisible = false;
    }

    private void installFormatter() {
        EditBox input = this.input();
        CommandSuggestionsAccessor accessor = (CommandSuggestionsAccessor)this.commandSuggestions();
        input.setFormatter((text, cursor) -> text.startsWith("/")
            ? accessor.twemojichat$invokeFormatChat(text, cursor)
            : EmojiTextRewriter.rewriteInput(text));
    }

    private EditBox input() {
        return ((ChatScreenAccessor)this.screen).twemojichat$getInput();
    }

    private CommandSuggestions commandSuggestions() {
        return ((ChatScreenAccessor)this.screen).twemojichat$getCommandSuggestions();
    }

    private void resizePopup(CommandSuggestionsAccessor accessor, List<Suggestion> suggestions, EmojiIndex index) {
        CommandSuggestions.SuggestionsList suggestionsList = accessor.twemojichat$getSuggestions();
        if (suggestionsList == null) {
            return;
        }

        Rect2i rect = ((CommandSuggestionsSuggestionsListAccessor)suggestionsList).twemojichat$getRect();
        int popupWidth = Math.max(0, this.popupWidth(accessor, suggestions, index));
        EditBox input = this.input();
        int maxX = Math.max(0, input.getScreenX(0) + input.getInnerWidth() - popupWidth);
        int x = Mth.clamp(input.getScreenX(this.token.start()), 0, maxX);
        rect.setX(x - (((EditBoxAccessor)input).twemojichat$isBordered() ? 0 : 1));
        rect.setWidth(popupWidth + 1);
    }

    private int popupWidth(CommandSuggestionsAccessor accessor, List<Suggestion> suggestions, EmojiIndex index) {
        if (this.cachedPopupWidth >= 0 && this.cachedPopupWidthIndex == index) {
            return this.cachedPopupWidth;
        }

        int width = 0;
        for (Suggestion suggestion : suggestions) {
            width = Math.max(width, accessor.twemojichat$getFont().width(this.displayComponent(suggestion, index)));
        }
        this.cachedPopupWidthIndex = index;
        this.cachedPopupWidth = width;
        return width;
    }

    private void primePopupWidth() {
        EmojiIndex index = EmojiIndexReloader.getIndex();
        if (index == EmojiIndex.EMPTY) {
            return;
        }

        CommandSuggestionsAccessor accessor = (CommandSuggestionsAccessor)this.commandSuggestions();
        this.cachedPopupWidth = this.popupWidth(accessor, index.complete("", index.size()));
        this.cachedPopupWidthIndex = index;
    }

    private int popupWidth(CommandSuggestionsAccessor accessor, List<EmojiDefinition> definitions) {
        int width = 0;
        for (EmojiDefinition definition : definitions) {
            width = Math.max(width, accessor.twemojichat$getFont().width(this.displayComponent(definition)));
        }
        return width;
    }

    private Component displayComponent(Suggestion suggestion, EmojiIndex index) {
        EmojiDefinition definition = this.definitionForSuggestion(suggestion, index);
        if (definition == null) {
            return Component.literal(suggestion.getText());
        }

        return this.displayComponent(definition);
    }

    private Component displayComponent(EmojiDefinition definition) {
        return Component.empty()
            .append(Component.literal(definition.glyph()).withStyle(EmojiFont.style()))
            .append(Component.literal(" :" + definition.primaryAlias() + ":"));
    }

    private EmojiDefinition definitionForSuggestion(Suggestion suggestion, EmojiIndex index) {
        String text = suggestion.getText();
        if (text.length() < 2 || text.charAt(0) != ':' || text.charAt(text.length() - 1) != ':') {
            return null;
        }

        return index.byAlias(text.substring(1, text.length() - 1));
    }

    private boolean replaceCompletedShortcode(String value, int cursor, EmojiIndex index) {
        ShortcodeMatch match = ShortcodeMatch.completed(value, cursor);
        if (match == null) {
            return false;
        }

        EmojiDefinition definition = index.byAlias(match.alias());
        if (definition == null) {
            return false;
        }

        String replacement = definition.glyph();
        String updated = value.substring(0, match.start()) + replacement + value.substring(match.end());
        int newCursor = match.start() + replacement.length();
        EditBox input = this.input();
        input.setValue(updated);
        input.setCursorPosition(newCursor);
        input.setHighlightPos(newCursor);
        this.lastValue = updated;
        this.lastCursor = newCursor;
        this.token = null;
        this.hideEmojiSuggestions(this.commandSuggestions());
        return true;
    }

    private record ActiveToken(int start, int end, String query) {
        private static ActiveToken find(String text, int cursor) {
            if (cursor <= 0 || cursor > text.length()) {
                return null;
            }

            if (ShortcodeMatch.completed(text, cursor) != null) {
                return null;
            }

            int start = cursor - 1;
            while (start >= 0 && isAliasCharacter(text.charAt(start))) {
                start--;
            }

            if (start < 0 || text.charAt(start) != ':' || start > 0 && text.charAt(start - 1) == ':') {
                return null;
            }

            for (int index = start + 1; index < cursor; index++) {
                if (!isAliasCharacter(text.charAt(index))) {
                    return null;
                }
            }

            int end = cursor;
            while (end < text.length() && isAliasCharacter(text.charAt(end))) {
                end++;
            }
            if (end < text.length() && text.charAt(end) == ':') {
                end++;
            }

            String query = text.substring(start + 1, cursor).toLowerCase(Locale.ROOT);
            return new ActiveToken(start, end, query);
        }

        private static boolean isAliasCharacter(char character) {
            return Character.isLetterOrDigit(character) || character == '_' || character == '+' || character == '-';
        }
    }

    private record ShortcodeMatch(int start, int end, String alias) {
        private static ShortcodeMatch completed(String text, int cursor) {
            if (cursor <= 1 || cursor > text.length()) {
                return null;
            }

            int end = cursor;
            if (end > 0 && text.charAt(end - 1) != ':') {
                return null;
            }

            int start = end - 2;
            while (start >= 0 && ActiveToken.isAliasCharacter(text.charAt(start))) {
                start--;
            }

            if (start < 0 || text.charAt(start) != ':' || start == end - 1 || start > 0 && text.charAt(start - 1) == ':') {
                return null;
            }

            String alias = text.substring(start + 1, end - 1).toLowerCase(Locale.ROOT);
            if (alias.isEmpty()) {
                return null;
            }

            return new ShortcodeMatch(start, end, alias);
        }
    }
}
