package com.irvin.casinovidas.niveles;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Hace aparecer estructuras guardadas con bloques de estructura (.nbt) cuando se activa un cambio. */
public final class NivelEstructuras {
    private NivelEstructuras() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String OVERWORLD = "minecraft:overworld";

    /** Cada cuántos ticks se coloca una pieza (5 = 4 piezas por segundo). */
    private static final int TICKS_ENTRE_PIEZAS = 5;

    /** Una pieza: nombre de la plantilla y su desplazamiento respecto a la esquina del sitio. */
    public record Pieza(String plantilla, int dx, int dy, int dz) {}

    /** Un sitio: dimensión, esquina mínima (x, y, z) de la estructura y sus piezas. */
    public record Sitio(String dim, int x, int y, int z, List<Pieza> piezas) {
        public String coords() { return x + ", " + y + ", " + z; }
    }

    private record Tarea(String dim, String plantilla, BlockPos pos) {}

    private static final ArrayDeque<Tarea> COLA = new ArrayDeque<>();

    /** Para usar como acción de un cambio: encola todas las piezas de los sitios. */
    public static Consumer<MinecraftServer> aparecer(Sitio... sitios) {
        return s -> {
            for (Sitio sitio : sitios) {
                for (Pieza p : sitio.piezas()) {
                    COLA.add(new Tarea(sitio.dim(), p.plantilla(),
                            new BlockPos(sitio.x() + p.dx(), sitio.y() + p.dy(), sitio.z() + p.dz())));
                }
            }
        };
    }

    public static void limpiar() {
        COLA.clear();
    }

    public static void tick(MinecraftServer s) {
        if (COLA.isEmpty() || s.getTickCount() % TICKS_ENTRE_PIEZAS != 0) return;
        Tarea t = COLA.poll();

        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(t.dim()));
        ServerLevel level = s.getLevel(key);
        if (level == null) {
            LOGGER.warn("[Niveles] Dimensión no encontrada: {}", t.dim());
            return;
        }

        Optional<StructureTemplate> tpl = level.getStructureManager().get(ResourceLocation.parse(t.plantilla()));
        if (tpl.isEmpty()) {
            LOGGER.warn("[Niveles] Plantilla no encontrada: {}", t.plantilla());
            return;
        }
        tpl.get().placeInWorld(level, t.pos(), t.pos(), new StructurePlaceSettings(), level.getRandom(), 2);
    }
}
