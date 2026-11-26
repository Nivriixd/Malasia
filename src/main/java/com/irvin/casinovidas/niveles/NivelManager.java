package com.irvin.casinovidas.niveles;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class NivelManager {
    private NivelManager() {}

    /** Ticks que pasan entre mostrar el círculo y aplicar el cambio (50 = 2.5 s, cuando el círculo se queda en su color). */
    private static final int DELAY_TICKS = 50;

    private record Pending(long runAt, NivelEvents.Def def) {}

    private static final List<Pending> PENDING = new ArrayList<>();

    public static NivelSavedData data(MinecraftServer s) {
        return s.overworld().getDataStorage().computeIfAbsent(NivelSavedData.factory(), "niveles");
    }

    public static Set<String> active(MinecraftServer s) {
        return data(s).active;
    }

    public static void clearPending() {
        PENDING.clear();
    }

    /** Muestra el círculo y programa el cambio. Devuelve false si ya está activo o en camino. */
    public static boolean activate(MinecraftServer s, NivelEvents.Def d) {
        String key = d.key();
        if (data(s).active.contains(key)) return false;
        for (Pending p : PENDING) {
            if (p.def().key().equals(key)) return false;
        }
        PENDING.add(new Pending(s.getTickCount() + DELAY_TICKS, d));
        PacketDistributor.sendToAllPlayers(new NivelCirculoPayload(d.color().ordinal()));
        return true;
    }

    private static void apply(MinecraftServer s, NivelEvents.Def d) {
        NivelSavedData dat = data(s);
        dat.active.add(d.key());
        dat.setDirty();
        d.alActivar().accept(s);
               s.getPlayerList().broadcastSystemMessage(
                Component.literal(d.titulo())
                        .withStyle(d.color().fmt, ChatFormatting.BOLD)
                        .append(Component.literal("\n" + d.descripcion()).withStyle(ChatFormatting.WHITE)),
                false);
    }

    public static void tick(MinecraftServer s) {
        if (!PENDING.isEmpty()) {
            long now = s.getTickCount();
            Iterator<Pending> it = PENDING.iterator();
            while (it.hasNext()) {
                Pending p = it.next();
                if (now >= p.runAt()) {
                    it.remove();
                    apply(s, p.def());
                }
            }
        }

        if (s.getTickCount() % 20 != 0) return;
        for (String key : data(s).active) {
            NivelEvents.Def d = NivelEvents.byKey(key);
            if (d != null && d.cadaSegundo() != null) d.cadaSegundo().accept(s);
        }
    }
}
