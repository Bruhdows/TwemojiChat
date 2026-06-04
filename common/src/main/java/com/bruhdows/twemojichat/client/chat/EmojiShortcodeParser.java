package com.bruhdows.twemojichat.client.chat;

import java.util.Locale;

public final class EmojiShortcodeParser {
  private EmojiShortcodeParser() {}

  public static ActiveToken findActiveToken(String text, int cursor) {
    if (cursor <= 0 || cursor > text.length()) {
      return null;
    }

    if (findCompletedShortcode(text, cursor) != null) {
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

  public static CompletedShortcode findCompletedShortcode(String text, int cursor) {
    if (cursor <= 1 || cursor > text.length()) {
      return null;
    }

    int end = cursor;
    if (text.charAt(end - 1) != ':') {
      return null;
    }

    int start = end - 2;
    while (start >= 0 && isAliasCharacter(text.charAt(start))) {
      start--;
    }

    if (start < 0
        || text.charAt(start) != ':'
        || start == end - 1
        || start > 0 && text.charAt(start - 1) == ':') {
      return null;
    }

    String alias = text.substring(start + 1, end - 1).toLowerCase(Locale.ROOT);
    if (alias.isEmpty()) {
      return null;
    }

    return new CompletedShortcode(start, end, alias);
  }

  static boolean isAliasCharacter(char character) {
    return Character.isLetterOrDigit(character)
        || character == '_'
        || character == '+'
        || character == '-';
  }

  public record ActiveToken(int start, int end, String query) {}

  public record CompletedShortcode(int start, int end, String alias) {}
}
