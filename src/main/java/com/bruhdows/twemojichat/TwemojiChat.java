package com.bruhdows.twemojichat;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(TwemojiChat.MOD_ID)
public final class TwemojiChat {
    public static final String MOD_ID = "twemojichat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TwemojiChat(IEventBus modEventBus) {
        TwemojiChatClient.init(modEventBus);
    }
}
