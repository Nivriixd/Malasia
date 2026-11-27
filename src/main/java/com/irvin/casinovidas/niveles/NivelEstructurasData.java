package com.irvin.casinovidas.niveles;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

/** Guarda las estructuras generadas (para poder borrarlas) y las bases registradas. */
public class NivelEstructurasData extends SavedData {

    public record Reg(int id, String dim, String estructura, BoundingBox bb) {}

    public final List<Reg> estructuras = new ArrayList<>();
    public final List<Long> bases = new ArrayList<>();
    private int siguienteId = 1;

    public static NivelEstructurasData get(MinecraftServer s) {
        return s.overworld().getDataStorage().computeIfAbsent(factory(), "niveles_estructuras");
    }

    public int registrar(String dim, String estructura, BoundingBox bb) {
        int id = siguienteId++;
        estructuras.add(new Reg(id, dim, estructura, bb));
        setDirty();
        return id;
    }

    public Reg buscar(int id) {
        for (Reg r : estructuras) if (r.id() == id) return r;
        return null;
    }

    public void quitar(int id) {
        estructuras.removeIf(r -> r.id() == id);
        setDirty();
    }

    public static SavedData.Factory<NivelEstructurasData> factory() {
        return new SavedData.Factory<>(NivelEstructurasData::new, NivelEstructurasData::load, null);
    }

    public static NivelEstructurasData load(CompoundTag tag, HolderLookup.Provider registries) {
        NivelEstructurasData d = new NivelEstructurasData();
        d.siguienteId = Math.max(1, tag.getInt("siguienteId"));

        ListTag list = tag.getList("estructuras", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            int[] a = c.getIntArray("bb");
            if (a.length != 6) continue;
            d.estructuras.add(new Reg(c.getInt("id"), c.getString("dim"), c.getString("estructura"),
                    new BoundingBox(a[0], a[1], a[2], a[3], a[4], a[5])));
        }

        for (long b : tag.getLongArray("bases")) d.bases.add(b);
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("siguienteId", siguienteId);

        ListTag list = new ListTag();
        for (Reg r : estructuras) {
            CompoundTag c = new CompoundTag();
            c.putInt("id", r.id());
            c.putString("dim", r.dim());
            c.putString("estructura", r.estructura());
            BoundingBox b = r.bb();
            c.putIntArray("bb", new int[]{b.minX(), b.minY(), b.minZ(), b.maxX(), b.maxY(), b.maxZ()});
            list.add(c);
        }
        tag.put("estructuras", list);

        long[] bases = new long[this.bases.size()];
        for (int i = 0; i < bases.length; i++) bases[i] = this.bases.get(i);
        tag.putLongArray("bases", bases);
        return tag;
    }
}
