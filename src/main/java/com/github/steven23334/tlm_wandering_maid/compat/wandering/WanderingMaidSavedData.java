package com.github.steven23334.tlm_wandering_maid.compat.wandering;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class WanderingMaidSavedData extends SavedData {
    public static final String DATA_NAME = "tlm_wandering_maid_wandering";

    /** ★ per-player 计时器：玩家 UUID → 下次允许触发事件的 gameTime */
    private final Map<UUID, Long> nextAttemptTicks = new HashMap<>();
    private final Map<UUID, List<String>> skinPools = new HashMap<>();

    public static WanderingMaidSavedData get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WanderingMaidSavedData::new,
                        WanderingMaidSavedData::load,
                        null),
                DATA_NAME);
    }

    public static WanderingMaidSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        WanderingMaidSavedData data = new WanderingMaidSavedData();

        // 兼容旧存档：曾经写过的单值 "NextAttemptTick" 被忽略（无 per-player 归属，只能丢弃）
        // 新版写 "NextAttemptTicks" 列表
        ListTag attempts = tag.getList("NextAttemptTicks", Tag.TAG_COMPOUND);
        for (int i = 0; i < attempts.size(); i++) {
            CompoundTag entry = attempts.getCompound(i);
            if (entry.hasUUID("Player")) {
                data.nextAttemptTicks.put(entry.getUUID("Player"), entry.getLong("Tick"));
            }
        }

        ListTag pools = tag.getList("SkinPools", Tag.TAG_COMPOUND);
        for (int i = 0; i < pools.size(); i++) {
            CompoundTag entry = pools.getCompound(i);
            if (!entry.hasUUID("Player")) {
                continue;
            }
            ListTag models = entry.getList("Models", Tag.TAG_STRING);
            List<String> ids = new ArrayList<>();
            for (int j = 0; j < models.size(); j++) {
                ids.add(models.getString(j));
            }
            data.skinPools.put(entry.getUUID("Player"), ids);
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        ListTag attempts = new ListTag();
        nextAttemptTicks.forEach((uuid, tick) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", uuid);
            entry.putLong("Tick", tick);
            attempts.add(entry);
        });
        tag.put("NextAttemptTicks", attempts);

        ListTag pools = new ListTag();
        skinPools.forEach((player, models) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", player);
            ListTag modelTags = new ListTag();
            models.forEach(id -> modelTags.add(StringTag.valueOf(id)));
            entry.put("Models", modelTags);
            pools.add(entry);
        });
        tag.put("SkinPools", pools);
        return tag;
    }

    /** 读某个玩家的下次触发时刻；没记录过返回 0。 */
    public long nextAttemptTick(UUID player) {
        return nextAttemptTicks.getOrDefault(player, 0L);
    }

    /** 写某个玩家的下次触发时刻。 */
    public void setNextAttemptTick(UUID player, long value) {
        nextAttemptTicks.put(player, value);
        setDirty();
    }

    /** 清理某个玩家的记录（可选：玩家被 ban / 永久移除时可调用）。 */
    public void clearNextAttempt(UUID player) {
        if (nextAttemptTicks.remove(player) != null) {
            setDirty();
        }
    }

    public List<String> skinPool(UUID player) {
        return List.copyOf(skinPools.getOrDefault(player, List.of()));
    }

    public void setSkinPool(UUID player, Collection<String> models) {
        Set<String> distinct = new LinkedHashSet<>(models);
        if (distinct.isEmpty()) {
            skinPools.remove(player);
        } else {
            skinPools.put(player, List.copyOf(distinct));
        }
        setDirty();
    }
}