package com.bruhdows.twemojichat.client.emoji;

import com.bruhdows.twemojichat.TwemojiChat;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public final class EmojiFont {
    public static final ResourceLocation ID = new ResourceLocation(TwemojiChat.MOD_ID, "emoji");

    private EmojiFont() {
    }

    public static Style style() {
        return Style.EMPTY.withFont(ID);
    }
}
