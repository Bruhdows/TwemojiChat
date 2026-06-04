package com.bruhdows.twemojichat.client.emoji;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmojiIndexTest {
  @Test
  void completesAliasesAndMatchesUnicode() {
    EmojiIndex index =
        EmojiIndex.load(
            new StringReader(
                """
            {
              "entries": [
                {
                  "aliases": ["smile", "happy"],
                  "primary_alias": "smile",
                  "glyph": "\\uE000",
                  "name": "Smile",
                  "sort_order": 2,
                  "unicode": "\\uD83D\\uDE04"
                },
                {
                  "aliases": ["sad"],
                  "primary_alias": "sad",
                  "glyph": "\\uE001",
                  "name": "Sad",
                  "sort_order": 1,
                  "unicode": "\\uD83D\\uDE22"
                }
              ]
            }
            """));

    List<EmojiDefinition> matches = index.complete("s", 10);
    assertEquals(
        List.of("sad", "smile"), matches.stream().map(EmojiDefinition::primaryAlias).toList());
    assertEquals("smile", index.byAlias("HAPPY").primaryAlias());

    EmojiIndex.EmojiUnicodeMatch unicodeMatch = index.matchUnicode("test \uD83D\uDE04!", 5);
    assertNotNull(unicodeMatch);
    assertEquals("smile", unicodeMatch.definition().primaryAlias());
    assertEquals(2, unicodeMatch.length());
  }
}
