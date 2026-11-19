package com.irvin.casinovidas.slotmachine.currency;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Balance de monedas por jugador (UUID). Vive solo en el servidor. */
public class CoinsSavedData extends SavedData {
    private final Map<UUID, Long> balances = new HashMap<>();

    public static SavedData.Factory<CoinsSavedData> factory() {
        return new SavedData.Factory<>(CoinsSavedData::new, CoinsSavedData::load, null);
    }

    public long get(UUID id) {
        return balances.getOrDefault(id, 0L);
    }

    public void set(UUID id, long value) {
        balances.put(id, Math.max(0L, value));
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Long> e : balances.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("id", e.getKey());
            t.putLong("coins", e.getValue());
            list.add(t);
        }
        tag.put("balances", list);
        return tag;
    }

    public static CoinsSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        CoinsSavedData data = new CoinsSavedData();
        ListTag list = tag.getList("balances", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            data.balances.put(t.getUUID("id"), t.getLong("coins"));
        }
        return data;
    }
}
