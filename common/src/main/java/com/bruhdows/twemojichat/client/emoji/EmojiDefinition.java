package com.bruhdows.twemojichat.client.emoji;

import java.util.List;

public record EmojiDefinition(
    List<String> aliases,
    String glyph,
    String name,
    String primaryAlias,
    int sortOrder,
    String unicodeValue) {}
