package com.irvin.casinovidas.niveles;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class NivelSavedData extends SavedData {
    public final Set<String> active = new LinkedHashSet<>();
    /** Jugadores que apagaron el buff de fuerza (por defecto está encendido para todos). */
    public final Set<UUID> fuerzaOff = new HashSet<>();
    /** Estado de las dimensiones que un cambio abrió o cerró (true = abierta). */
    public final Map<String, Boolean> dimState = new HashMap<>();

    public static SavedData.Factory<NivelSavedData> factory() {
        return new SavedData.Factory<>(NivelSavedData::new, NivelSavedData::load, null);
    }

    public static NivelSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        NivelSavedData d = new NivelSavedData();
        ListTag list = tag.getList("active", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) d.active.add(list.getString(i));

        ListTag off = tag.getList("fuerzaOff", Tag.TAG_STRING);
        for (int i = 0; i < off.size(); i++) {
            try {
                d.fuerzaOff.add(UUID.fromString(off.getString(i)));
            } catch (IllegalArgumentException ignored) {}
        }

        CompoundTag dims = tag.getCompound("dimState");
        for (String k : dims.getAllKeys()) d.dimState.put(k, dims.getBoolean(k));
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (String k : active) list.add(StringTag.valueOf(k));
        tag.put("active", list);

        ListTag off = new ListTag();
        for (UUID u : fuerzaOff) off.add(StringTag.valueOf(u.toString()));
        tag.put("fuerzaOff", off);

        CompoundTag dims = new CompoundTag();
        for (Map.Entry<String, Boolean> e : dimState.entrySet()) dims.putBoolean(e.getKey(), e.getValue());
        tag.put("dimState", dims);
        return tag;
    }
}
