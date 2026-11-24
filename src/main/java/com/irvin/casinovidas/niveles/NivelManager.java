package com.irvin.casinovidas.niveles;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Set;

public final class NivelManager {
    private NivelManager() {}

    private static NivelSavedData data(MinecraftServer s) {
        return s.overworld().getDataStorage().computeIfAbsent(NivelSavedData.factory(), "niveles");
    }

    public static Set<String> active(MinecraftServer s) {
        return data(s).active;
    }

    /** Devuelve false si el evento ya estaba activo. */
    public static boolean activate(MinecraftServer s, NivelEvents.Def d) {
        NivelSavedData dat = data(s);
        if (!dat.active.add(d.key())) return false;
        dat.setDirty();
        d.alActivar().accept(s);
        PacketDistributor.sendToAllPlayers(new NivelCirculoPayload(d.color().ordinal()));
        s.getPlayerList().broadcastSystemMessage(
                Component.literal("[" + d.color().id.toUpperCase() + "] " + d.nombre()), false);
        return true;
    }

    public static void reset(MinecraftServer s) {
        NivelSavedData dat = data(s);
        dat.active.clear();
        dat.setDirty();
    }

    /** Una vez por segundo ejecuta los efectos repetitivos de los eventos activos. */
    public static void tick(MinecraftServer s) {
        if (s.getTickCount() % 20 != 0) return;
        for (String key : data(s).active) {
            NivelEvents.Def d = NivelEvents.byKey(key);
            if (d != null && d.cadaSegundo() != null) d.cadaSegundo().accept(s);
        }
    }
}
