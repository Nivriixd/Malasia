package com.irvin.casinovidas.niveles;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class NivelEstructuras {
    private NivelEstructuras() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String OVERWORLD = "minecraft:overworld";

    // ------------------------- AJUSTES -------------------------
    /** Distancia mínima (bloques) a cualquier base o jugador. */
    private static final int DIST_MIN_BASES = 400;
    /** Zona de búsqueda alrededor del spawn del mundo. */
    private static final int RADIO_MIN_SPAWN = 500;
    private static final int RADIO_MAX_SPAWN = 3000;
    /** Intentos de generar una estructura válida. */
    private static final int INTENTOS = 10;
    private static final int TICKS_ENTRE_CHUNKS = 2;
    private static final int TICKS_ENTRE_BORRADO = 4;
    private static final int TICKS_ENTRE_PIEZAS = 5;

    // ----------------- PLANTILLAS .nbt EN SITIOS FIJOS -----------------
    public record Pieza(String plantilla, int dx, int dy, int dz) {}

    public record Sitio(String dim, int x, int y, int z, List<Pieza> piezas) {
        public String coords() { return x + ", " + y + ", " + z; }
    }

    private record Tarea(String dim, String plantilla, BlockPos pos) {}

    private static final ArrayDeque<Tarea> COLA = new ArrayDeque<>();

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

    // ------------- ESTRUCTURAS (MODS O VANILLA) EN PUNTO ALEATORIO -------------
    /** Estructura registrada (por ejemplo "dungeons_arise:nombre") que aparece lejos de las bases. */
    public record Aleatoria(String dim, String estructura) {}

    private static final class Trabajo {
        final ServerLevel level;
        final StructureStart start;
        final ArrayDeque<ChunkPos> chunks;

        Trabajo(ServerLevel level, StructureStart start, ArrayDeque<ChunkPos> chunks) {
            this.level = level;
            this.start = start;
            this.chunks = chunks;
        }
    }

    private static final class Borrado {
        final int id;
        final ServerLevel level;
        final BoundingBox bb;
        final ArrayDeque<ChunkPos> chunks;

        Borrado(int id, ServerLevel level, BoundingBox bb, ArrayDeque<ChunkPos> chunks) {
            this.id = id;
            this.level = level;
            this.bb = bb;
            this.chunks = chunks;
        }
    }

    private static final ArrayDeque<Trabajo> COLOCAR = new ArrayDeque<>();
    private static final ArrayDeque<Borrado> BORRAR = new ArrayDeque<>();
    private static final List<String> COORDS = new ArrayList<>();

    /** Coordenadas de lo generado en esta activación, para reemplazar {1}, {2}... en la descripción del cambio. */
    public static List<String> consumirCoords() {
        List<String> c = new ArrayList<>(COORDS);
        COORDS.clear();
        return c;
    }

    /** Acción para un cambio: genera cada estructura en un punto aleatorio lejos de las bases. */
    public static Consumer<MinecraftServer> aleatoria(Aleatoria... items) {
        return s -> {
            for (Aleatoria a : items) {
                COORDS.add(aparecerAleatoria(s, a.dim(), ResourceLocation.parse(a.estructura())));
            }
        };
    }

    /** Devuelve las coordenadas elegidas ("x, y, z") o "?" si no se pudo. */
    public static String aparecerAleatoria(MinecraftServer s, String dim, ResourceLocation id) {
        ServerLevel level = s.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dim)));
        if (level == null) {
            avisarOps(s, "Dimensión no encontrada: " + dim);
            return "?";
        }
        Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(id);
        if (structure == null) {
            avisarOps(s, "Estructura no encontrada: " + id);
            return "?";
        }

        ChunkGenerator gen = level.getChunkSource().getGenerator();
        RandomSource rnd = level.getRandom();

        for (int i = 0; i < INTENTOS; i++) {
            BlockPos p = elegirPunto(s, level, rnd);
            if (p == null) break;

            StructureStart start = structure.generate(level.registryAccess(), gen, gen.getBiomeSource(),
                    level.getChunkSource().randomState(), level.getStructureManager(), level.getSeed(),
                    new ChunkPos(p), 0, level, biome -> true);
            if (!start.isValid()) continue;

            BoundingBox bb = start.getBoundingBox();
            ChunkPos min = new ChunkPos(SectionPos.blockToSectionCoord(bb.minX()), SectionPos.blockToSectionCoord(bb.minZ()));
            ChunkPos max = new ChunkPos(SectionPos.blockToSectionCoord(bb.maxX()), SectionPos.blockToSectionCoord(bb.maxZ()));
            ArrayDeque<ChunkPos> chunks = new ArrayDeque<>();
            ChunkPos.rangeClosed(min, max).forEach(chunks::add);

            COLOCAR.add(new Trabajo(level, start, chunks));
            NivelEstructurasData.get(s).registrar(dim, id.toString(), bb);

            BlockPos c = bb.getCenter();
            return c.getX() + ", " + c.getY() + ", " + c.getZ();
        }

        avisarOps(s, "No se pudo ubicar " + id + " (sin punto válido). Revisa los ajustes de distancia.");
        return "?";
    }

    /** Pone en cola el borrado de una estructura registrada. false si ya se está borrando. */
    public static boolean borrar(MinecraftServer s, NivelEstructurasData.Reg r) {
        for (Borrado b : BORRAR) if (b.id == r.id()) return false;
        ServerLevel level = s.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(r.dim())));
        if (level == null) return false;

        BoundingBox bb = r.bb();
        ChunkPos min = new ChunkPos(SectionPos.blockToSectionCoord(bb.minX()), SectionPos.blockToSectionCoord(bb.minZ()));
        ChunkPos max = new ChunkPos(SectionPos.blockToSectionCoord(bb.maxX()), SectionPos.blockToSectionCoord(bb.maxZ()));
        ArrayDeque<ChunkPos> chunks = new ArrayDeque<>();
        ChunkPos.rangeClosed(min, max).forEach(chunks::add);
        BORRAR.add(new Borrado(r.id(), level, bb, chunks));
        return true;
    }

    private static List<BlockPos> referencias(MinecraftServer s) {
        List<BlockPos> r = new ArrayList<>();
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            r.add(p.blockPosition());
            BlockPos rp = p.getRespawnPosition();
            if (rp != null) r.add(rp);
        }
        for (long b : NivelEstructurasData.get(s).bases) r.add(BlockPos.of(b));
        return r;
    }

    private static BlockPos elegirPunto(MinecraftServer s, ServerLevel level, RandomSource rnd) {
        BlockPos spawn = level.getSharedSpawnPos();
        List<BlockPos> refs = referencias(s);
        long minSq = (long) DIST_MIN_BASES * DIST_MIN_BASES;

        for (int i = 0; i < 30; i++) {
            double ang = rnd.nextDouble() * Math.PI * 2;
            double dist = RADIO_MIN_SPAWN + rnd.nextDouble() * (RADIO_MAX_SPAWN - RADIO_MIN_SPAWN);
            int x = spawn.getX() + (int) (Math.cos(ang) * dist);
            int z = spawn.getZ() + (int) (Math.sin(ang) * dist);
            BlockPos p = new BlockPos(x, 64, z);

            if (!level.getWorldBorder().isWithinBounds(p)) continue;

            boolean lejos = true;
            for (BlockPos r : refs) {
                long dx = r.getX() - x;
                long dz = r.getZ() - z;
                if (dx * dx + dz * dz < minSq) {
                    lejos = false;
                    break;
                }
            }
            if (!lejos) continue;

            Holder<Biome> biome = level.getBiome(p);
            if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) continue;

            return p;
        }
        return null;
    }

    private static void avisarOps(MinecraftServer s, String msg) {
        LOGGER.warn("[Niveles] {}", msg);
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            if (s.getPlayerList().isOp(p.getGameProfile())) p.sendSystemMessage(Component.literal("[Niveles] " + msg));
        }
    }

    // ------------------------------ TICK ------------------------------
    public static void limpiar() {
        COLA.clear();
        COLOCAR.clear();
        BORRAR.clear();
        COORDS.clear();
    }

    public static void tick(MinecraftServer s) {
        int t = s.getTickCount();

        // 1) Colocar estructuras aleatorias, un chunk cada pocos ticks
        if (!COLOCAR.isEmpty() && t % TICKS_ENTRE_CHUNKS == 0) {
            Trabajo w = COLOCAR.peek();
            ChunkPos cp = w.chunks.poll();
            if (cp != null) {
                w.level.getChunk(cp.x, cp.z); // fuerza que el chunk exista
                w.start.placeInChunk(w.level, w.level.structureManager(), w.level.getChunkSource().getGenerator(),
                        w.level.getRandom(),
                        new BoundingBox(cp.getMinBlockX(), w.level.getMinBuildHeight(), cp.getMinBlockZ(),
                                cp.getMaxBlockX(), w.level.getMaxBuildHeight(), cp.getMaxBlockZ()),
                        cp);
            }
            if (w.chunks.isEmpty()) COLOCAR.poll();
        }

        // 2) Borrar estructuras, un chunk cada pocos ticks
        if (!BORRAR.isEmpty() && t % TICKS_ENTRE_BORRADO == 0) {
            Borrado b = BORRAR.peek();
            ChunkPos cp = b.chunks.poll();
            if (cp != null) borrarChunk(b, cp);
            if (b.chunks.isEmpty()) {
                BORRAR.poll();
                NivelEstructurasData.get(s).quitar(b.id);
            }
        }

        // 3) Piezas de plantillas .nbt
        if (COLA.isEmpty() || t % TICKS_ENTRE_PIEZAS != 0) return;
        Tarea tarea = COLA.poll();

        ServerLevel level = s.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tarea.dim())));
        if (level == null) {
            LOGGER.warn("[Niveles] Dimensión no encontrada: {}", tarea.dim());
            return;
        }
        Optional<StructureTemplate> tpl = level.getStructureManager().get(ResourceLocation.parse(tarea.plantilla()));
        if (tpl.isEmpty()) {
            LOGGER.warn("[Niveles] Plantilla no encontrada: {}", tarea.plantilla());
            return;
        }
        tpl.get().placeInWorld(level, tarea.pos(), tarea.pos(), new StructurePlaceSettings(), level.getRandom(), 2);
    }

    private static void borrarChunk(Borrado b, ChunkPos cp) {
        ServerLevel level = b.level;
        level.getChunk(cp.x, cp.z);

        int x0 = Math.max(cp.getMinBlockX(), b.bb.minX());
        int x1 = Math.min(cp.getMaxBlockX(), b.bb.maxX());
        int z0 = Math.max(cp.getMinBlockZ(), b.bb.minZ());
        int z1 = Math.min(cp.getMaxBlockZ(), b.bb.maxZ());
        int y0 = b.bb.minY();
        int y1 = b.bb.maxY();

        AABB area = new AABB(x0, y0, z0, x1 + 1, y1 + 1, z1 + 1);
        for (Entity e : level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof Player))) {
            e.discard();
        }

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
        for (int y = y1; y >= y0; y--) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    pos.set(x, y, z);
                    BlockState st = level.getBlockState(pos);
                    if (st.isAir()) continue;
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be != null) Clearable.tryClear(be); // que no suelte el contenido de cofres
                    level.setBlock(pos, air, flags);
                }
            }
        }
    }
}
