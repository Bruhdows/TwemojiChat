package com.bruhdows.twemojichat.client.emoji;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class EmojiTextRewriter {
    private EmojiTextRewriter() {
    }

    public static Component rewrite(Component original) {
        EmojiIndex index = EmojiIndexReloader.getIndex();
        if (index == EmojiIndex.EMPTY) {
            return original;
        }

        MutableComponent rewritten = Component.empty();
        boolean changed = false;

        for (Component flatComponent : original.toFlatList()) {
            RewriteResult result = rewriteSegment(flatComponent.getString(), flatComponent.getStyle(), index);
            changed |= result.changed();
            for (Component piece : result.components()) {
                rewritten.append(piece);
            }
        }

        return changed ? rewritten : original;
    }

    private static RewriteResult rewriteSegment(String text, Style style, EmojiIndex index) {
        List<Component> components = new ArrayList<>();
        StringBuilder pending = new StringBuilder();
        boolean changed = false;
        int cursor = 0;

        while (cursor < text.length()) {
            int shortcodeEnd = shortcodeEnd(text, cursor);
            if (shortcodeEnd > cursor) {
                String alias = text.substring(cursor + 1, shortcodeEnd - 1).toLowerCase(Locale.ROOT);
                EmojiDefinition definition = index.byAlias(alias);
                if (definition != null) {
                    flushPending(components, pending, style);
                    components.add(Component.literal(definition.glyph()).withStyle(emojiStyle(style)));
                    cursor = shortcodeEnd;
                    changed = true;
                    continue;
                }
            }

            EmojiIndex.EmojiUnicodeMatch unicodeMatch = index.matchUnicode(text, cursor);
            if (unicodeMatch != null) {
                flushPending(components, pending, style);
                components.add(Component.literal(unicodeMatch.definition().glyph()).withStyle(emojiStyle(style)));
                cursor += unicodeMatch.length();
                changed = true;
                continue;
            }

            int codePoint = text.codePointAt(cursor);
            pending.appendCodePoint(codePoint);
            cursor += Character.charCount(codePoint);
        }

        flushPending(components, pending, style);
        if (components.isEmpty()) {
            components.add(Component.literal(text).withStyle(style));
        }

        return new RewriteResult(List.copyOf(components), changed);
    }

    private static Style emojiStyle(Style baseStyle) {
        return Style.EMPTY
            .withClickEvent(baseStyle.getClickEvent())
            .withHoverEvent(baseStyle.getHoverEvent())
            .withInsertion(baseStyle.getInsertion())
            .withFont(EmojiFont.ID);
    }

    private static void flushPending(List<Component> components, StringBuilder pending, Style style) {
        if (pending.isEmpty()) {
            return;
        }

        components.add(Component.literal(pending.toString()).withStyle(style));
        pending.setLength(0);
    }

    private static int shortcodeEnd(String text, int start) {
        if (text.charAt(start) != ':') {
            return -1;
        }

        int cursor = start + 1;
        while (cursor < text.length() && isAliasCharacter(text.charAt(cursor))) {
            cursor++;
        }

        if (cursor == start + 1 || cursor >= text.length() || text.charAt(cursor) != ':') {
            return -1;
        }

        return cursor + 1;
    }

    private static boolean isAliasCharacter(char character) {
        return Character.isLetterOrDigit(character) || character == '_' || character == '+' || character == '-';
    }

    private record RewriteResult(List<Component> components, boolean changed) {
    }
}
