package com.flechazo.sky_accessories;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SkyAccessoriesSavedData extends net.minecraft.world.level.saveddata.SavedData {
    private UUID owner;

    public static SkyAccessoriesSavedData get(Level level) {
        if (!(level instanceof ServerLevel sl)) return null;
        ServerLevel overworld = sl.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return null;
        return overworld.getDataStorage().computeIfAbsent(SkyAccessoriesSavedData::load, SkyAccessoriesSavedData::new, "sky_accessories_data");
    }

    public SkyAccessoriesSavedData() {
    }

    public static SkyAccessoriesSavedData load(CompoundTag tag) {
        SkyAccessoriesSavedData d = new SkyAccessoriesSavedData();
        if (tag.hasUUID("owner")) d.owner = tag.getUUID("owner");
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (owner != null) tag.putUUID("owner", owner);
        return tag;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID uuid) {
        this.owner = uuid;
        this.setDirty();
    }
}