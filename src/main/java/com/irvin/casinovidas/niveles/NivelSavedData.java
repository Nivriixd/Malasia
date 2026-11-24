package com.irvin.casinovidas.niveles;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashSet;
import java.util.Set;

public class NivelSavedData extends SavedData {
    public final Set<String> active = new LinkedHashSet<>();

    public static SavedData.Factory<NivelSavedData> factory() {
        return new SavedData.Factory<>(NivelSavedData::new, NivelSavedData::load, null);
    }

    public static NivelSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        NivelSavedData d = new NivelSavedData();
        ListTag list = tag.getList("active", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) d.active.add(list.getString(i));
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (String k : active) list.add(StringTag.valueOf(k));
        tag.put("active", list);
        return tag;
    }
}
