package com.bruhdows.twemojichat.client.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class EmojiShortcodeParserTest {
  @Test
  void findsCompletedShortcodeAtCursor() {
    EmojiShortcodeParser.CompletedShortcode match =
        EmojiShortcodeParser.findCompletedShortcode(":smile:", 7);

    assertNotNull(match);
    assertEquals(0, match.start());
    assertEquals(7, match.end());
    assertEquals("smile", match.alias());
  }

  @Test
  void ignoresDoubleColonPrefix() {
    assertNull(EmojiShortcodeParser.findCompletedShortcode("::smile:", 8));
    assertNull(EmojiShortcodeParser.findActiveToken("::sm", 4));
  }

  @Test
  void findsActiveTokenWithoutClosingColon() {
    EmojiShortcodeParser.ActiveToken token = EmojiShortcodeParser.findActiveToken("hi :smi", 7);

    assertNotNull(token);
    assertEquals(3, token.start());
    assertEquals(7, token.end());
    assertEquals("smi", token.query());
  }
}
