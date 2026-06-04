package com.bruhdows.twemojichat.version;

import net.minecraft.util.FormattedCharSequence;

@FunctionalInterface
public interface InputFormatter {
  FormattedCharSequence format(String text, int cursor);
}
