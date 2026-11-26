package com.irvin.casinovidas.niveles;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class NivelEvents {
    private NivelEvents() {}

    /** alActivar = se ejecuta una vez. cadaSegundo = se repite cada segundo mientras esté activo (puede ser null). */
    public record Def(NivelColor color, int numero, String titulo, String descripcion,
                      Consumer<MinecraftServer> alActivar, Consumer<MinecraftServer> cadaSegundo) {
        public String key() { return color.id + "_" + numero; }
    }

    public static final List<Def> ALL = new ArrayList<>();

    static {
        // ---------- VERDE (fácil) ----------
        msg(NivelColor.VERDE, 1, "Tarea del día", "Consigue 6 diamantes y reclama tu recompensa.");
        msg(NivelColor.VERDE, 2, "Recomendación", "Construye tu base para protegerte y recolectar recursos.");
        msg(NivelColor.VERDE, 3, "Tarea del día", "Consigue 3 bloques de hierro para reclamar tu recompensa.");
        msg(NivelColor.VERDE, 4, "¡DÍA LIBRE!", "El día de hoy será libre, puedes terminar tu casa, crear tu propio automóvil, sigue recolectando recursos, etc. Suerte.");
        msg(NivelColor.VERDE, 5, "Recordatorio", "Puedes poner la música que quieras, dónde quieras y cuándo quieras.");
        msg(NivelColor.VERDE, 6, "Aviso", "La tienda ha actualizado su contenido.");
        msg(NivelColor.VERDE, 7, "DÍA LIBRE", "Puedes explorar el espacio, jugar minijuegos, mejorar o terminar la base.");
        msg(NivelColor.VERDE, 8, "Tarea del día", "Mata 12 mobs y recibe tu recompensa.");
        msg(NivelColor.VERDE, 9, "Recordatorio", "Puedes decorar tu casa en modo navidad. Nuevas decoraciones para navidad, diseñadas para la creatividad (puede servir de algo para más adelante)...");
        msg(NivelColor.VERDE, 10, "Recordatorio", "Puedes crear tu \"televisor\" para ver YouTube y otras páginas web.");
        msg(NivelColor.VERDE, 11, "Tarea del día", "Mata 20 mobs y consigue tu recompensa.");
        msg(NivelColor.VERDE, 12, "Casino habilitado", "Se habilita el casino, puedes convertir tus monedas en digital o mantenerlas en físico para usar las máquinas.");
        msg(NivelColor.VERDE, 13, "DÍA LIBRE", "Puedes reformar tu base, crear tus propios peluches, practicar en la pista de carreras, etc.");
        msg(NivelColor.VERDE, 14, "Tarea del día", "Consigue 2 stacks de hueso y reclama tu recompensa.");
        // 15 (dimensión "The sift"): pendiente, tiene mecánica
        msg(NivelColor.VERDE, 16, "Tarea del día", "Consigue 2 bloques de diamante y reclama tu recompensa.");
        msg(NivelColor.VERDE, 17, "Aviso", "La tienda se ha actualizado, puede que lo necesites.");
        msg(NivelColor.VERDE, 18, "Tarea del día", "Consigue 3 bloques de esmeralda y reclama tu recompensa.");
        // 19 (tienda -50%): pendiente, tiene mecánica
        msg(NivelColor.VERDE, 20, "Tarea del día", "Califica las construcciones de los demás y recoge tu recompensa.");
        add(NivelColor.VERDE, 21, "Buff", "Les cae un buff de fuerza a todos los jugadores, que se puede activar y desactivar cuando ellos quieran, con una sola tecla (G por defecto).",
                s -> {}, NivelFuerza::tick);
        msg(NivelColor.VERDE, 22, "Tarea del día", "Mata 1 mob y recibe tu recompensa.");
        // 23 (nether): pendiente, tiene mecánica
        msg(NivelColor.VERDE, 24, "¡Aviso!", "El creador (Nivriixd) quiere tomar una foto grupal por ser posiblemente el último día. Reunirse todos en el Spawn.");
    }

    /** Cambio que solo informa por chat. */
    private static void msg(NivelColor c, int n, String titulo, String descripcion) {
        ALL.add(new Def(c, n, titulo, descripcion, s -> {}, null));
    }

    /** Cambio con acción en el servidor (para los que tienen mecánica). */
    private static void add(NivelColor c, int n, String titulo, String descripcion,
                            Consumer<MinecraftServer> on, Consumer<MinecraftServer> tick) {
        ALL.add(new Def(c, n, titulo, descripcion, on, tick));
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
