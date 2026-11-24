package com.irvin.casinovidas.niveles;

import net.minecraft.ChatFormatting;

public enum NivelColor {
    VERDE("verde", ChatFormatting.GREEN),
    AMARILLO("amarillo", ChatFormatting.YELLOW),
    ROJO("rojo", ChatFormatting.RED);

    public final String id;
    public final ChatFormatting fmt;

    NivelColor(String id, ChatFormatting fmt) {
        this.id = id;
        this.fmt = fmt;
    }
}
