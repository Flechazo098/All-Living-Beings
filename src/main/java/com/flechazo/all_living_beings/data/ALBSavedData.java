package com.flechazo.all_living_beings.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ALBSavedData extends SavedData {
    private final Map<ResourceLocation, BlockPos> boundPositions = new HashMap<>();
    private UUID owner;

    public ALBSavedData() {
    }

    public static ALBSavedData get(Level level) {
        if (!(level instanceof ServerLevel sl)) return null;
        ServerLevel overworld = sl.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return null;
        return overworld.getDataStorage().computeIfAbsent(ALBSavedData::load, ALBSavedData::new, "all_living_beings_data");
    }

    public static ALBSavedData load(CompoundTag tag) {
        ALBSavedData d = new ALBSavedData();
        if (tag.hasUUID("owner")) d.owner = tag.getUUID("owner");
        if (tag.contains("bounds")) {
            CompoundTag bounds = tag.getCompound("bounds");
            for (String key : bounds.getAllKeys()) {
                ResourceLocation id = new ResourceLocation(key);
                BlockPos pos = NbtUtils.readBlockPos(bounds.getCompound(key));
                d.boundPositions.put(id, pos);
            }
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (owner != null) tag.putUUID("owner", owner);
        if (!boundPositions.isEmpty()) {
            CompoundTag bounds = new CompoundTag();
            for (var e : boundPositions.entrySet()) {
                bounds.put(e.getKey().toString(), NbtUtils.writeBlockPos(e.getValue()));
            }
            tag.put("bounds", bounds);
        }
        return tag;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID uuid) {
        this.owner = uuid;
        this.setDirty();
    }

    public BlockPos getBound(ResourceLocation id) {
        return boundPositions.get(id);
    }

    public void setBound(ResourceLocation id, BlockPos pos) {
        boundPositions.put(id, pos);
        this.setDirty();
    }
}