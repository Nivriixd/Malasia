package com.irvin.casinovidas.niveles;

import net.minecraft.server.MinecraftServer;

import java.util.Set;

/** Abrir y cerrar dimensiones desde los cambios. */
public final class NivelDims {
    private NivelDims() {}

    public static final String NETHER = "minecraft:the_nether";
    public static final String SIFT = "the_sift:the_sift";

    /** Dimensiones que empiezan cerradas hasta que un cambio las abra. */
    private static final Set<String> CERRADAS_POR_DEFECTO = Set.of(SIFT);

    public static boolean isOpen(MinecraftServer s, String dim) {
        Boolean st = NivelManager.data(s).dimState.get(dim);
        return st != null ? st : !CERRADAS_POR_DEFECTO.contains(dim);
    }

    public static void abrir(MinecraftServer s, String dim) {
        set(s, dim, true);
    }

    public static void cerrar(MinecraftServer s, String dim) {
        set(s, dim, false);
    }

    private static void set(MinecraftServer s, String dim, boolean open) {
        NivelSavedData d = NivelManager.data(s);
        d.dimState.put(dim, open);
        d.setDirty();
    }
}
