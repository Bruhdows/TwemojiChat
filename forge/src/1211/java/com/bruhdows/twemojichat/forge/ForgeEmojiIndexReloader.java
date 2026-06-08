package com.bruhdows.twemojichat.forge;

import com.bruhdows.twemojichat.client.TwemojiChatClientEntrypoint;
import com.bruhdows.twemojichat.client.emoji.EmojiIndex;
import com.bruhdows.twemojichat.client.emoji.EmojiIndexReloader;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

public final class ForgeEmojiIndexReloader extends SimplePreparableReloadListener<EmojiIndex> {
  private final TwemojiChatClientEntrypoint entrypoint;

  public ForgeEmojiIndexReloader(TwemojiChatClientEntrypoint entrypoint) {
    this.entrypoint = entrypoint;
  }

  @Override
  protected @NotNull EmojiIndex prepare(
      @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
    return EmojiIndexReloader.load(resourceManager);
  }

  @Override
  protected void apply(
      EmojiIndex prepared,
      @NotNull ResourceManager resourceManager,
      @NotNull ProfilerFiller profiler) {
    EmojiIndexReloader.apply(prepared);
    this.entrypoint.onEmojiIndexReload(resourceManager);
  }
}
