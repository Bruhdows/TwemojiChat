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

public final class EmojiIndex {
    public static final EmojiIndex EMPTY = new EmojiIndex(Map.of(), List.of(), new UnicodeNode());

    private final Map<String, EmojiDefinition> byAlias;
    private final List<EmojiDefinition> suggestions;
    private final UnicodeNode unicodeRoot;

    private EmojiIndex(Map<String, EmojiDefinition> byAlias, List<EmojiDefinition> suggestions, UnicodeNode unicodeRoot) {
        this.byAlias = byAlias;
        this.suggestions = suggestions;
        this.unicodeRoot = unicodeRoot;
    }

    public static EmojiIndex load(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonArray entries = root.getAsJsonArray("entries");
        Map<String, EmojiDefinition> byAlias = new HashMap<>();
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

            String primaryAlias = entryObject.get("primary_alias").isJsonNull() ? null : entryObject.get("primary_alias").getAsString().toLowerCase(Locale.ROOT);
            EmojiDefinition definition = new EmojiDefinition(
                List.copyOf(aliases),
                entryObject.get("glyph").getAsString(),
                entryObject.get("name").getAsString(),
                primaryAlias,
                entryObject.get("sort_order").getAsInt(),
                entryObject.get("unicode").getAsString()
            );

            for (String alias : aliases) {
                byAlias.putIfAbsent(alias, definition);
            }

            if (primaryAlias != null) {
                suggestions.add(definition);
            }

            insertUnicode(unicodeRoot, definition);
        }

        suggestions.sort(Comparator.comparingInt(EmojiDefinition::sortOrder).thenComparing(EmojiDefinition::primaryAlias));
        return new EmojiIndex(Map.copyOf(byAlias), List.copyOf(suggestions), unicodeRoot);
    }

    public List<EmojiDefinition> complete(String query, int limit) {
        if (this.suggestions.isEmpty()) {
            return List.of();
        }

        if (query.isEmpty()) {
            return this.suggestions.subList(0, Math.min(limit, this.suggestions.size()));
        }

        String normalized = query.toLowerCase(Locale.ROOT);
        List<EmojiDefinition> results = new ArrayList<>(limit);
        EmojiDefinition exact = this.byAlias.get(normalized);
        if (exact != null && exact.primaryAlias() != null) {
            results.add(exact);
        }

        for (EmojiDefinition definition : this.suggestions) {
            if (results.size() >= limit) {
                break;
            }
            if (definition == exact) {
                continue;
            }
            if (matchesPrefix(definition, normalized)) {
                results.add(definition);
            }
        }

        return results;
    }

    public EmojiDefinition byAlias(String alias) {
        return this.byAlias.get(alias.toLowerCase(Locale.ROOT));
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

    private static boolean matchesPrefix(EmojiDefinition definition, String normalizedQuery) {
        for (String alias : definition.aliases()) {
            if (alias.startsWith(normalizedQuery)) {
                return true;
            }
        }
        return false;
    }

    public record EmojiUnicodeMatch(EmojiDefinition definition, int length) {
    }

    private static final class UnicodeNode {
        private final Map<Integer, UnicodeNode> children = new HashMap<>();
        private EmojiDefinition definition;
    }
}
