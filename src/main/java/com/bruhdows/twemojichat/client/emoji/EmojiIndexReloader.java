package com.bruhdows.twemojichat.client.emoji;

import com.bruhdows.twemojichat.TwemojiChat;
import java.io.IOException;
import java.io.Reader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.NotNull;

public final class EmojiIndexReloader extends SimplePreparableReloadListener<EmojiIndex> {
    private static final ResourceLocation INDEX_RESOURCE = ResourceLocation.fromNamespaceAndPath(TwemojiChat.MOD_ID, "twemoji/index.json");
    private static volatile EmojiIndex index = EmojiIndex.EMPTY;

    public static EmojiIndex getIndex() {
        return index;
    }

    public static void register(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new EmojiIndexReloader());
    }

    @Override
    protected @NotNull EmojiIndex prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        try (Reader reader = resourceManager.openAsReader(INDEX_RESOURCE)) {
            return EmojiIndex.load(reader);
        } catch (IOException exception) {
            TwemojiChat.LOGGER.error("Failed to load Twemoji index {}", INDEX_RESOURCE, exception);
            return EmojiIndex.EMPTY;
        }
    }

    @Override
    protected void apply(EmojiIndex prepared, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        index = prepared;
        TwemojiChat.LOGGER.info("Loaded {} Twemoji definitions", prepared.size());
    }
}
