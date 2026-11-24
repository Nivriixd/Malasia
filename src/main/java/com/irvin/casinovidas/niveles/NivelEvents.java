package com.irvin.casinovidas.niveles;

import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameRules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class NivelEvents {
    private NivelEvents() {}

    /** alActivar = se ejecuta una vez. cadaSegundo = se repite cada segundo mientras esté activo (puede ser null). */
    public record Def(NivelColor color, int numero, String nombre,
                      Consumer<MinecraftServer> alActivar, Consumer<MinecraftServer> cadaSegundo) {
        public String key() { return color.id + "_" + numero; }
    }

    public static final List<Def> ALL = new ArrayList<>();

    static {
        add(NivelColor.VERDE, 1, "Dificultad fácil", s -> s.setDifficulty(Difficulty.EASY, true), null);
        add(NivelColor.VERDE, 2, "Regeneración para todos", s -> {}, s -> effect(s, MobEffects.REGENERATION));
        add(NivelColor.VERDE, 3, "Mantener inventario al morir",
                s -> s.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, s), null);
        add(NivelColor.VERDE, 4, "Clima despejado",
                s -> s.overworld().setWeatherParameters(12000, 0, false, false), null);
      
        add(NivelColor.AMARILLO, 1, "Dificultad normal", s -> s.setDifficulty(Difficulty.NORMAL, true), null);
        add(NivelColor.AMARILLO, 2, "Hambre para todos", s -> {}, s -> effect(s, MobEffects.HUNGER));
        add(NivelColor.AMARILLO, 3, "Lluvia",
                s -> s.overworld().setWeatherParameters(0, 12000, true, false), null);
        add(NivelColor.AMARILLO, 4, "Ya no se mantiene el inventario",
                s -> s.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(false, s), null);

        add(NivelColor.ROJO, 1, "Dificultad difícil", s -> s.setDifficulty(Difficulty.HARD, true), null);
        add(NivelColor.ROJO, 2, "Debilidad para todos", s -> {}, s -> effect(s, MobEffects.WEAKNESS));
        add(NivelColor.ROJO, 3, "Tormenta",
                s -> s.overworld().setWeatherParameters(0, 12000, true, true), null);
        add(NivelColor.ROJO, 4, "Jugadores brillando", s -> {}, s -> effect(s, MobEffects.GLOWING));
    }

    private static void add(NivelColor c, int n, String nombre, Consumer<MinecraftServer> on, Consumer<MinecraftServer> tick) {
        ALL.add(new Def(c, n, nombre, on, tick));
    }

    private static void effect(MinecraftServer s, Holder<MobEffect> eff) {
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            p.addEffect(new MobEffectInstance(eff, 80, 0, true, false));
        }
    }

    public static List<Def> of(NivelColor c) {
        return ALL.stream().filter(d -> d.color() == c).toList();
    }

    public static Def find(NivelColor c, int n) {
        for (Def d : ALL) if (d.color() == c && d.numero() == n) return d;
        return null;
    }

    public static Def byKey(String key) {
        for (Def d : ALL) if (d.key().equals(key)) return d;
        return null;
    }
}
