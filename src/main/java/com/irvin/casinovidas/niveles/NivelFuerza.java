package com.irvin.casinovidas.niveles;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class NivelFuerza {
    private NivelFuerza() {}

    public static final String KEY = "verde_21";
    private static final int AMPLIFIER = 0; // 0 = Fuerza I, 1 = Fuerza II

    private static MobEffectInstance buff() {
        return new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, AMPLIFIER, true, false, true);
    }

    /** Se llama cada segundo mientras el cambio 21 está activo. */
    public static void tick(MinecraftServer s) {
        NivelSavedData d = NivelManager.data(s);
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            if (!d.fuerzaOff.contains(p.getUUID())) p.addEffect(buff());
        }
    }

    public static void toggle(ServerPlayer p) {
        MinecraftServer s = p.server;
        if (!NivelManager.active(s).contains(KEY)) {
            p.displayClientMessage(Component.literal("Aún no tienes el buff de fuerza"), true);
            return;
        }
        NivelSavedData d = NivelManager.data(s);
        boolean nowOn;
        if (d.fuerzaOff.remove(p.getUUID())) {
            nowOn = true;
        } else {
            d.fuerzaOff.add(p.getUUID());
            nowOn = false;
        }
        d.setDirty();

        if (nowOn) p.addEffect(buff());
        else p.removeEffect(MobEffects.DAMAGE_BOOST);

        p.displayClientMessage(Component.literal("Fuerza: " + (nowOn ? "activada" : "desactivada"))
                .withStyle(nowOn ? ChatFormatting.GREEN : ChatFormatting.RED), true);
    }
}
