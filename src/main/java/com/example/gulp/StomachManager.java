/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.world.level.saveddata.SavedData
 */
package com.example.gulp;

import com.example.gulp.Stomach;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class StomachManager
extends SavedData {
    private final Map<UUID, Stomach> map = new HashMap<UUID, Stomach>();

    public static StomachManager get(MinecraftServer server) {
        return (StomachManager)server.m_129783_().m_8895_().m_164861_(StomachManager::load, StomachManager::new, "gulp_stomachs");
    }

    public Stomach of(UUID id) {
        return this.map.computeIfAbsent(id, k -> new Stomach());
    }

    public CompoundTag m_7176_(CompoundTag nbt) {
        CompoundTag players = new CompoundTag();
        this.map.forEach((id, stomach) -> players.m_128365_(id.toString(), (Tag)stomach.toNbt()));
        nbt.m_128365_("Players", (Tag)players);
        return nbt;
    }

    public static StomachManager load(CompoundTag nbt) {
        StomachManager m = new StomachManager();
        CompoundTag players = nbt.m_128469_("Players");
        for (String key : players.m_128431_()) {
            m.map.put(UUID.fromString(key), Stomach.fromNbt(players.m_128469_(key)));
        }
        return m;
    }
}

