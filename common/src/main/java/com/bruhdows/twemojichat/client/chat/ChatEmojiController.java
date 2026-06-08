package com.bruhdows.twemojichat.client.chat;

import com.bruhdows.twemojichat.client.emoji.EmojiDefinition;
import com.bruhdows.twemojichat.client.emoji.EmojiFont;
import com.bruhdows.twemojichat.client.emoji.EmojiIndex;
import com.bruhdows.twemojichat.client.emoji.EmojiIndexReloader;
import com.bruhdows.twemojichat.client.emoji.EmojiTextRewriter;
import com.bruhdows.twemojichat.mixin.client.ChatScreenAccessor;
import com.bruhdows.twemojichat.mixin.client.CommandSuggestionsAccessor;
import com.bruhdows.twemojichat.mixin.client.CommandSuggestionsSuggestionsListAccessor;
import com.bruhdows.twemojichat.version.VersionHooks;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class ChatEmojiController {
  private final ChatScreen screen;
  private EmojiShortcodeParser.ActiveToken token;
  private String lastValue = "";
  private int lastCursor = -1;
  private boolean emojiSuggestionsVisible;
  private List<Suggestion> suggestions = List.of();
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
    this.refresh(value, cursor, value.equals(this.lastValue) && cursor == this.lastCursor);
  }

  public boolean tryCommitCurrentSuggestion() {
    EditBox input = this.input();
    this.refresh(input.getValue(), input.getCursorPosition(), false);

    Suggestion suggestion = this.selectedSuggestion();
    if (suggestion == null) {
      return false;
    }

    this.applySuggestion(suggestion);
    return true;
  }

  private void refresh(String value, int cursor, boolean allowCached) {
    if (allowCached && value.equals(this.lastValue) && cursor == this.lastCursor) {
      return;
    }

    this.lastValue = value;
    this.lastCursor = cursor;

    CommandSuggestions commandSuggestions = this.commandSuggestions();
    if (value.startsWith("/")) {
      this.clearState();
      this.hideEmojiSuggestions(commandSuggestions);
      return;
    }

    EmojiIndex index = EmojiIndexReloader.getIndex();
    if (this.replaceCompletedShortcode(value, cursor, index)) {
      return;
    }

    this.token = EmojiShortcodeParser.findActiveToken(value, cursor);
    if (this.token == null) {
      this.suggestions = List.of();
      this.hideEmojiSuggestions(commandSuggestions);
      return;
    }

    List<EmojiDefinition> matches = index.complete(this.token.query(), index.size());
    if (matches.isEmpty()) {
      this.suggestions = List.of();
      this.hideEmojiSuggestions(commandSuggestions);
      this.token = null;
      return;
    }

    List<Suggestion> suggestionsList = matches.stream().map(this::toSuggestion).toList();
    this.suggestions = suggestionsList;
    Suggestions suggestions =
        new Suggestions(StringRange.between(this.token.start(), this.token.end()), suggestionsList);
    CommandSuggestionsAccessor accessor = (CommandSuggestionsAccessor) commandSuggestions;
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

    CommandSuggestionsAccessor accessor = (CommandSuggestionsAccessor) commandSuggestions;
    accessor.twemojichat$setPendingSuggestions(Suggestions.empty());
    accessor.twemojichat$invokeHide();
    this.input().setSuggestion(null);
    this.emojiSuggestionsVisible = false;
  }

  private void clearState() {
    this.token = null;
    this.suggestions = List.of();
  }

  private void installFormatter() {
    EditBox input = this.input();
    CommandSuggestionsAccessor accessor = (CommandSuggestionsAccessor) this.commandSuggestions();
    VersionHooks.INSTANCE.installInputFormatter(
        input,
        (text, cursor) ->
            text.startsWith("/")
                ? accessor.twemojichat$invokeFormatChat(text, cursor)
                : EmojiTextRewriter.rewriteInput(text));
  }

  private EditBox input() {
    return ((ChatScreenAccessor) this.screen).twemojichat$getInput();
  }

  private CommandSuggestions commandSuggestions() {
    return ((ChatScreenAccessor) this.screen).twemojichat$getCommandSuggestions();
  }

  private void resizePopup(
      CommandSuggestionsAccessor accessor, List<Suggestion> suggestions, EmojiIndex index) {
    CommandSuggestions.SuggestionsList suggestionsList = accessor.twemojichat$getSuggestions();
    if (suggestionsList == null) {
      return;
    }

    Rect2i rect =
        ((CommandSuggestionsSuggestionsListAccessor) suggestionsList).twemojichat$getRect();
    int popupWidth = Math.max(0, this.popupWidth(accessor, suggestions, index));
    EditBox input = this.input();
    int maxX = Math.max(0, input.getScreenX(0) + input.getInnerWidth() - popupWidth);
    int x = Mth.clamp(input.getScreenX(this.token.start()), 0, maxX);
    rect.setX(x - (VersionHooks.INSTANCE.isInputBordered(input) ? 0 : 1));
    rect.setWidth(popupWidth + 1);
  }

  private int popupWidth(
      CommandSuggestionsAccessor accessor, List<Suggestion> suggestions, EmojiIndex index) {
    if (this.cachedPopupWidth >= 0 && this.cachedPopupWidthIndex == index) {
      return this.cachedPopupWidth;
    }

    int width = 0;
    for (Suggestion suggestion : suggestions) {
      width =
          Math.max(
              width,
              accessor.twemojichat$getFont().width(this.displayComponent(suggestion, index)));
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

    CommandSuggestionsAccessor accessor = (CommandSuggestionsAccessor) this.commandSuggestions();
    this.cachedPopupWidth = this.popupWidth(accessor, index.complete("", index.size()));
    this.cachedPopupWidthIndex = index;
  }

  private int popupWidth(CommandSuggestionsAccessor accessor, List<EmojiDefinition> definitions) {
    int width = 0;
    for (EmojiDefinition definition : definitions) {
      width =
          Math.max(width, accessor.twemojichat$getFont().width(this.displayComponent(definition)));
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

  private Suggestion selectedSuggestion() {
    CommandSuggestions.SuggestionsList suggestionsList =
        ((CommandSuggestionsAccessor) this.commandSuggestions()).twemojichat$getSuggestions();
    if (suggestionsList != null) {
      CommandSuggestionsSuggestionsListAccessor accessor =
          (CommandSuggestionsSuggestionsListAccessor) suggestionsList;
      List<Suggestion> visibleSuggestions = accessor.twemojichat$getSuggestionList();
      if (this.areEmojiSuggestions(visibleSuggestions)) {
        int current = accessor.twemojichat$getCurrent();
        if (current >= 0 && current < visibleSuggestions.size()) {
          return visibleSuggestions.get(current);
        }
        if (!visibleSuggestions.isEmpty()) {
          return visibleSuggestions.get(0);
        }
      }
    }

    if (!this.suggestions.isEmpty()) {
      return this.suggestions.get(0);
    }

    return null;
  }

  private boolean areEmojiSuggestions(List<Suggestion> suggestions) {
    return !suggestions.isEmpty() && suggestions.stream().allMatch(this::isEmojiSuggestion);
  }

  private boolean isEmojiSuggestion(Suggestion suggestion) {
    String text = suggestion.getText();
    return text.length() >= 2 && text.charAt(0) == ':' && text.charAt(text.length() - 1) == ':';
  }

  private void applySuggestion(Suggestion suggestion) {
    if (this.token == null) {
      return;
    }

    EditBox input = this.input();
    String value = input.getValue();
    String replacement = suggestion.apply(value);
    int newCursor = this.token.start() + suggestion.getText().length();
    input.setValue(replacement);
    input.setCursorPosition(newCursor);
    input.setHighlightPos(newCursor);
    this.lastValue = replacement;
    this.lastCursor = newCursor;
    this.replaceCompletedShortcode(replacement, newCursor, EmojiIndexReloader.getIndex());
  }

  private boolean replaceCompletedShortcode(String value, int cursor, EmojiIndex index) {
    EmojiShortcodeParser.CompletedShortcode match =
        EmojiShortcodeParser.findCompletedShortcode(value, cursor);
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
    this.clearState();
    this.hideEmojiSuggestions(this.commandSuggestions());
    return true;
  }
}
