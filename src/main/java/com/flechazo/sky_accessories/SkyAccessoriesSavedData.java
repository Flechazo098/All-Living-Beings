package com.flechazo.sky_accessories;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SkyAccessoriesSavedData extends net.minecraft.world.level.saveddata.SavedData {
    private UUID owner;
    private final Map<ResourceLocation, BlockPos> boundPositions = new HashMap<>();
    private Boolean godCmdsEnabledOverride;

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
        // 加载绑定传送点
        if (tag.contains("bounds")) {
            CompoundTag bounds = tag.getCompound("bounds");
            for (String key : bounds.getAllKeys()) {
                ResourceLocation id = new ResourceLocation(key);
                BlockPos pos = NbtUtils.readBlockPos(bounds.getCompound(key));
                d.boundPositions.put(id, pos);
            }
        }
        // 加载 god 命令开关覆盖
        if (tag.contains("god_cmds_enabled_override")) {
            d.godCmdsEnabledOverride = tag.getBoolean("god_cmds_enabled_override");
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (owner != null) tag.putUUID("owner", owner);
        // 保存绑定传送点
        if (!boundPositions.isEmpty()) {
            CompoundTag bounds = new CompoundTag();
            for (var e : boundPositions.entrySet()) {
                bounds.put(e.getKey().toString(), NbtUtils.writeBlockPos(e.getValue()));
            }
            tag.put("bounds", bounds);
        }
        // 保存 god 命令开关覆盖
        if (godCmdsEnabledOverride != null) {
            tag.putBoolean("god_cmds_enabled_override", godCmdsEnabledOverride);
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
    // 新增 API
    public Optional<Boolean> getGodCommandsEnabledOverride() { return Optional.ofNullable(godCmdsEnabledOverride); }
    public void setGodCommandsEnabledOverride(Boolean b) { this.godCmdsEnabledOverride = b; this.setDirty(); }
    public BlockPos getBound(ResourceLocation id) { return boundPositions.get(id); }
    public void setBound(ResourceLocation id, BlockPos pos) { boundPositions.put(id, pos); this.setDirty(); }
}