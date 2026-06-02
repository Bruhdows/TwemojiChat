package com.bruhdows.twemojichat.client.chat;

import com.bruhdows.twemojichat.client.emoji.EmojiDefinition;
import com.bruhdows.twemojichat.client.emoji.EmojiFont;
import com.bruhdows.twemojichat.client.emoji.EmojiIndex;
import com.bruhdows.twemojichat.client.emoji.EmojiIndexReloader;
import com.bruhdows.twemojichat.mixin.client.ChatScreenAccessor;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;

public final class ChatEmojiController {
    private static final int BACKGROUND_COLOR = 0xD0101010;
    private static final int BORDER_COLOR = 0xFF3F3F46;
    private static final int HIGHLIGHT_COLOR = 0xFF2563EB;
    private static final int MAX_SUGGESTIONS = 8;
    private static final int PADDING = 4;
    private static final int ROW_HEIGHT = 12;

    private final ChatScreen screen;
    private ActiveToken token;
    private int popupHeight;
    private int popupWidth;
    private int popupX;
    private int popupY;
    private int selectedIndex;
    private List<EmojiDefinition> suggestions = List.of();
    private String lastValue = "";
    private int lastCursor = -1;

    public ChatEmojiController(ChatScreen screen) {
        this.screen = screen;
    }

    public boolean handleKeyPressed(int keyCode) {
        this.refresh();
        if (!this.isVisible()) {
            return false;
        }

        if (keyCode == GLFW.GLFW_KEY_TAB) {
            this.accept(this.selectedIndex);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_UP) {
            this.selectedIndex = Math.floorMod(this.selectedIndex - 1, this.suggestions.size());
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            this.selectedIndex = Math.floorMod(this.selectedIndex + 1, this.suggestions.size());
            return true;
        }

        return false;
    }

    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        this.refresh();
        if (!this.isVisible() || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }

        if (mouseX < this.popupX || mouseX > this.popupX + this.popupWidth || mouseY < this.popupY || mouseY > this.popupY + this.popupHeight) {
            return false;
        }

        int row = (int)((mouseY - this.popupY - PADDING) / ROW_HEIGHT);
        if (row < 0 || row >= this.suggestions.size()) {
            return false;
        }

        this.accept(row);
        return true;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        this.refresh();
        if (!this.isVisible()) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        guiGraphics.fill(this.popupX, this.popupY, this.popupX + this.popupWidth, this.popupY + this.popupHeight, BACKGROUND_COLOR);
        guiGraphics.renderOutline(this.popupX, this.popupY, this.popupWidth, this.popupHeight, BORDER_COLOR);

        for (int index = 0; index < this.suggestions.size(); index++) {
            int rowTop = this.popupY + PADDING + index * ROW_HEIGHT;
            if (index == this.selectedIndex) {
                guiGraphics.fill(this.popupX + 1, rowTop - 1, this.popupX + this.popupWidth - 1, rowTop + ROW_HEIGHT - 1, HIGHLIGHT_COLOR);
            }

            guiGraphics.drawString(font, this.labelFor(this.suggestions.get(index)), this.popupX + PADDING, rowTop + 1, 0xFFFFFFFF, false);
        }
    }

    private void accept(int index) {
        if (this.token == null || index < 0 || index >= this.suggestions.size()) {
            return;
        }

        EditBox input = this.input();
        EmojiDefinition definition = this.suggestions.get(index);
        String replacement = ":" + definition.primaryAlias() + ":";
        String value = input.getValue();
        String updated = value.substring(0, this.token.start()) + replacement + value.substring(this.token.end());
        int cursor = this.token.start() + replacement.length();

        input.setValue(updated);
        input.setCursorPosition(cursor);
        input.setHighlightPos(cursor);

        this.lastValue = "";
        this.lastCursor = -1;
        this.refresh();
    }

    private EditBox input() {
        return ((ChatScreenAccessor)this.screen).twemojichat$getInput();
    }

    private boolean isVisible() {
        return this.token != null && !this.suggestions.isEmpty();
    }

    private void refresh() {
        EditBox input = this.input();
        String value = input.getValue();
        int cursor = input.getCursorPosition();
        if (value.equals(this.lastValue) && cursor == this.lastCursor) {
            return;
        }

        this.lastValue = value;
        this.lastCursor = cursor;
        this.token = ActiveToken.find(value, cursor);
        this.suggestions = List.of();

        if (this.token == null) {
            return;
        }

        EmojiIndex index = EmojiIndexReloader.getIndex();
        this.suggestions = index.complete(this.token.query(), MAX_SUGGESTIONS);
        if (this.suggestions.isEmpty()) {
            this.token = null;
            return;
        }

        this.selectedIndex = Math.min(this.selectedIndex, this.suggestions.size() - 1);
        this.selectedIndex = Math.max(this.selectedIndex, 0);
        this.layoutPopup(input);
    }

    private void layoutPopup(EditBox input) {
        Font font = Minecraft.getInstance().font;
        int widestLabel = this.suggestions.stream().mapToInt(definition -> font.width(this.labelFor(definition))).max().orElse(0);
        this.popupWidth = Math.max(120, widestLabel + PADDING * 2);
        this.popupHeight = this.suggestions.size() * ROW_HEIGHT + PADDING * 2;
        this.popupX = input.getX();
        this.popupY = input.getY() - this.popupHeight - 4;
        if (this.popupY < 4) {
            this.popupY = input.getY() + input.getHeight() + 4;
        }
    }

    private net.minecraft.network.chat.Component labelFor(EmojiDefinition definition) {
        return net.minecraft.network.chat.Component.empty()
            .append(net.minecraft.network.chat.Component.literal(":" + definition.primaryAlias() + ": "))
            .append(net.minecraft.network.chat.Component.literal(definition.glyph()).withStyle(EmojiFont.style()));
    }

    private record ActiveToken(int start, int end, String query) {
        private static ActiveToken find(String text, int cursor) {
            if (cursor <= 0 || cursor > text.length()) {
                return null;
            }

            if (text.charAt(cursor - 1) == ':') {
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
}
