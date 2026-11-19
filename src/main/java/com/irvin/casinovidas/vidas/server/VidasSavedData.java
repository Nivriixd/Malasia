package com.irvin.casinovidas.vidas.server;

import com.irvin.casinovidas.vidas.VidasRules;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Guarda vidas por jugador (UUID) en el mundo. Nada de esto vive en el cliente. */
public class VidasSavedData extends SavedData {
    public static class Entry {
        public int lives = VidasRules.MAX_LIVES;
        public int goldSeconds = 0;
    }

    private final Map<UUID, Entry> map = new HashMap<>();

    public static SavedData.Factory<VidasSavedData> factory() {
        return new SavedData.Factory<>(VidasSavedData::new, VidasSavedData::load, null);
    }

    public Entry get(UUID id) {
        return map.computeIfAbsent(id, k -> {
            setDirty();
            return new Entry();
        });
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Entry> e : map.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("id", e.getKey());
            t.putInt("lives", e.getValue().lives);
            t.putInt("gold", e.getValue().goldSeconds);
            list.add(t);
        }
        tag.put("players", list);
        return tag;
    }

    public static VidasSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        VidasSavedData data = new VidasSavedData();
        ListTag list = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            Entry e = new Entry();
            e.lives = t.getInt("lives");
            e.goldSeconds = t.getInt("gold");
            data.map.put(t.getUUID("id"), e);
        }
        return data;
    }
}
