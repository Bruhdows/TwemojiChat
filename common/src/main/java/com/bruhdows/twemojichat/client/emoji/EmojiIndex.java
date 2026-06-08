package com.bruhdows.twemojichat.client.emoji;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class EmojiIndex {
  private static final int NO_MATCH = Integer.MAX_VALUE;
  public static final EmojiIndex EMPTY =
      new EmojiIndex(Map.of(), Map.of(), List.of(), new UnicodeNode());

  private final Map<String, EmojiDefinition> byAlias;
  private final Map<String, EmojiDefinition> byGlyph;
  private final List<EmojiDefinition> suggestions;
  private final UnicodeNode unicodeRoot;

  private EmojiIndex(
      Map<String, EmojiDefinition> byAlias,
      Map<String, EmojiDefinition> byGlyph,
      List<EmojiDefinition> suggestions,
      UnicodeNode unicodeRoot) {
    this.byAlias = byAlias;
    this.byGlyph = byGlyph;
    this.suggestions = suggestions;
    this.unicodeRoot = unicodeRoot;
  }

  public static EmojiIndex load(Reader reader) {
    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
    JsonArray entries = root.getAsJsonArray("entries");
    Map<String, EmojiDefinition> byAlias = new HashMap<>();
    Map<String, EmojiDefinition> byGlyph = new HashMap<>();
    List<EmojiDefinition> suggestions = new ArrayList<>();
    UnicodeNode unicodeRoot = new UnicodeNode();

    for (JsonElement element : entries) {
      JsonObject entryObject = element.getAsJsonObject();
      List<String> aliases = new ArrayList<>();
      if (entryObject.has("aliases")) {
        for (JsonElement aliasElement : entryObject.getAsJsonArray("aliases")) {
          aliases.add(aliasElement.getAsString().toLowerCase(Locale.ROOT));
        }
      }

      String primaryAlias =
          entryObject.get("primary_alias").isJsonNull()
              ? null
              : entryObject.get("primary_alias").getAsString().toLowerCase(Locale.ROOT);
      EmojiDefinition definition =
          new EmojiDefinition(
              List.copyOf(aliases),
              entryObject.get("glyph").getAsString(),
              entryObject.get("name").getAsString(),
              primaryAlias,
              entryObject.get("sort_order").getAsInt(),
              entryObject.get("unicode").getAsString());

      for (String alias : aliases) {
        byAlias.putIfAbsent(alias, definition);
      }
      byGlyph.putIfAbsent(definition.glyph(), definition);

      if (primaryAlias != null) {
        suggestions.add(definition);
      }

      insertUnicode(unicodeRoot, definition);
    }

    suggestions.sort(
        Comparator.comparingInt(EmojiDefinition::sortOrder)
            .thenComparing(EmojiDefinition::primaryAlias));
    return new EmojiIndex(
        Map.copyOf(byAlias), Map.copyOf(byGlyph), List.copyOf(suggestions), unicodeRoot);
  }

  public List<EmojiDefinition> complete(String query, int limit) {
    if (this.suggestions.isEmpty()) {
      return List.of();
    }

    if (query.isEmpty()) {
      return this.suggestions.subList(0, Math.min(limit, this.suggestions.size()));
    }

    String normalized = query.toLowerCase(Locale.ROOT);
    List<RankedEmoji> matches = new ArrayList<>(this.suggestions.size());
    for (EmojiDefinition definition : this.suggestions) {
      MatchQuality quality = this.matchQuality(definition, normalized);
      if (quality.score() != NO_MATCH) {
        matches.add(new RankedEmoji(definition, quality));
      }
    }

    matches.sort(
        Comparator.comparingInt((RankedEmoji ranked) -> ranked.quality().score())
            .thenComparingInt(ranked -> ranked.quality().aliasIndex())
            .thenComparingInt(ranked -> ranked.quality().aliasLength())
            .thenComparingInt(ranked -> ranked.definition().sortOrder())
            .thenComparing(
                ranked -> ranked.definition().primaryAlias(),
                Comparator.nullsLast(String::compareTo)));

    List<EmojiDefinition> results = new ArrayList<>(Math.min(limit, matches.size()));
    for (RankedEmoji ranked : matches) {
      if (results.size() >= limit) {
        break;
      }
      results.add(ranked.definition());
    }

    return results;
  }

  public EmojiDefinition byAlias(String alias) {
    return this.byAlias.get(alias.toLowerCase(Locale.ROOT));
  }

  public EmojiDefinition byGlyph(String glyph) {
    return this.byGlyph.get(glyph);
  }

  public EmojiUnicodeMatch matchUnicode(String text, int startIndex) {
    UnicodeNode node = this.unicodeRoot;
    EmojiDefinition match = null;
    int matchEnd = startIndex;
    int cursor = startIndex;

    while (cursor < text.length()) {
      int codePoint = text.codePointAt(cursor);
      node = node.children.get(codePoint);
      if (node == null) {
        break;
      }

      cursor += Character.charCount(codePoint);
      if (node.definition != null) {
        match = node.definition;
        matchEnd = cursor;
      }
    }

    if (match == null) {
      return null;
    }

    return new EmojiUnicodeMatch(match, matchEnd - startIndex);
  }

  public int size() {
    return this.suggestions.size();
  }

  private static void insertUnicode(UnicodeNode root, EmojiDefinition definition) {
    UnicodeNode node = root;
    String unicode = definition.unicodeValue();
    for (int index = 0; index < unicode.length(); ) {
      int codePoint = unicode.codePointAt(index);
      node = node.children.computeIfAbsent(codePoint, ignored -> new UnicodeNode());
      index += Character.charCount(codePoint);
    }
    node.definition = definition;
  }

  private MatchQuality matchQuality(EmojiDefinition definition, String query) {
    String normalizedQuery = searchKey(query);
    MatchQuality best = new MatchQuality(NO_MATCH, Integer.MAX_VALUE, Integer.MAX_VALUE);
    List<String> aliases = definition.aliases();

    for (int index = 0; index < aliases.size(); index++) {
      String alias = aliases.get(index);
      int score = matchScore(alias, query, normalizedQuery);
      if (score == NO_MATCH) {
        continue;
      }

      MatchQuality candidate = new MatchQuality(score, index, alias.length());
      if (best.compareTo(candidate) > 0) {
        best = candidate;
      }
    }

    return best;
  }

  private static int matchScore(String alias, String query, String normalizedQuery) {
    if (alias.equals(query)) {
      return 0;
    }
    if (alias.startsWith(query)) {
      return 1;
    }

    String normalizedAlias = searchKey(alias);
    if (normalizedAlias.equals(normalizedQuery)) {
      return 2;
    }
    if (normalizedAlias.startsWith(normalizedQuery)) {
      return 3;
    }

    return NO_MATCH;
  }

  private static String searchKey(String value) {
    StringBuilder builder = new StringBuilder(value.length());
    for (int index = 0; index < value.length(); index++) {
      char character = value.charAt(index);
      if (Character.isLetterOrDigit(character)) {
        builder.append(Character.toLowerCase(character));
      }
    }
    return builder.toString();
  }

  public record EmojiUnicodeMatch(EmojiDefinition definition, int length) {}

  private record MatchQuality(int score, int aliasIndex, int aliasLength)
      implements Comparable<MatchQuality> {
    @Override
    public int compareTo(MatchQuality other) {
      return Comparator.comparingInt(MatchQuality::score)
          .thenComparingInt(MatchQuality::aliasIndex)
          .thenComparingInt(MatchQuality::aliasLength)
          .compare(this, other);
    }
  }

  private record RankedEmoji(EmojiDefinition definition, MatchQuality quality) {
    private RankedEmoji {
      Objects.requireNonNull(definition);
      Objects.requireNonNull(quality);
    }
  }

  private static final class UnicodeNode {
    private final Map<Integer, UnicodeNode> children = new HashMap<>();
    private EmojiDefinition definition;
  }
}
