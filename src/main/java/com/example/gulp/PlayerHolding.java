/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Component$Serializer
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.example.gulp;

import com.example.gulp.GulpServerConfig;
import com.example.gulp.GulpSound;
import com.example.gulp.Stomach;
import com.example.gulp.StomachLogic;
import com.example.gulp.StomachManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PlayerHolding {
    private static final String KEY_PREV_MODE = "gulp_prev_mode";
    private static final Map<UUID, Held> HELD = new HashMap<UUID, Held>();

    private PlayerHolding() {
    }

    public static boolean isHeld(ServerPlayer p) {
        return HELD.containsKey(p.m_20148_());
    }

    public static boolean isPlayerEntry(CompoundTag entry) {
        return entry.m_128441_("PlayerUuid");
    }

    public static boolean swallow(ServerPlayer p, Stomach s, ServerPlayer victim, StomachManager mgr) {
        if (!((Boolean)GulpServerConfig.PLAYER_SWALLOWING_ENABLED.get()).booleanValue()) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Swallowing players is turned off on this server."));
            return false;
        }
        if (!victim.m_6084_() || victim.m_5833_() || PlayerHolding.isHeld(victim)) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Nothing swallowable in reach."));
            return false;
        }
        if (!mgr.of((UUID)victim.m_20148_()).allowSwallowed) {
            StomachLogic.say(p, (Component)Component.m_237119_().m_7220_(victim.m_7755_()).m_130946_(" hasn't allowed being swallowed."));
            return false;
        }
        float size = Math.max(0.05f, victim.m_20205_() * victim.m_20205_() * victim.m_20206_());
        if ((double)size > s.capacity()) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)("Too big for your stomach! (needs " + Stomach.fmtVolume(size) + ", capacity " + Stomach.fmtVolume(s.capacity()) + ")")));
            return false;
        }
        if (s.used() + (double)size > s.capacity()) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Your stomach is too full."));
            return false;
        }
        if (s.contents.size() >= 100) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"You can't hold more than 100 creatures at once."));
            return false;
        }
        CompoundTag entry = new CompoundTag();
        CompoundTag display = new CompoundTag();
        display.m_128359_("id", "minecraft:player");
        display.m_128350_("Health", victim.m_21223_());
        entry.m_128365_("Entity", (Tag)display);
        entry.m_128359_("PlayerUuid", victim.m_20148_().toString());
        entry.m_128350_("Size", size);
        entry.m_128350_("MaxHealth", victim.m_21233_());
        entry.m_128405_("Digest", 0);
        entry.m_128405_("DigestTime", 0);
        Component name = victim.m_7755_();
        entry.m_128359_("Name", name.getString());
        entry.m_128359_("NameJson", Component.Serializer.m_130703_((Component)name));
        entry.m_128405_("Uid", p.m_217043_().m_188502_());
        entry.m_128405_("HeldTicks", 0);
        s.contents.add(entry);
        int previous = victim.f_8941_.m_9290_().m_46392_();
        HELD.put(victim.m_20148_(), new Held(p.m_20148_(), previous));
        victim.getPersistentData().m_128405_(KEY_PREV_MODE, previous);
        victim.m_143403_(GameType.SPECTATOR);
        victim.m_9213_((Entity)p);
        victim.m_213846_((Component)Component.m_237113_((String)"You were swallowed by ").m_7220_(p.m_7755_()).m_130946_(". Press your Release key (default G) to get out whenever you like."));
        return true;
    }

    public static boolean releaseEntry(ServerPlayer swallower, CompoundTag entry) {
        UUID id = PlayerHolding.victimId(entry);
        if (id == null) {
            return true;
        }
        Held held = HELD.remove(id);
        ServerPlayer victim = swallower.m_20194_().m_6846_().m_11259_(id);
        if (victim != null) {
            PlayerHolding.restore(victim, held, swallower);
        }
        return true;
    }

    public static void escape(ServerPlayer victim) {
        Held held = HELD.get(victim.m_20148_());
        if (held == null) {
            return;
        }
        MinecraftServer server = victim.m_20194_();
        ServerPlayer holder = server.m_6846_().m_11259_(held.swallower());
        StomachManager mgr = StomachManager.get(server);
        Stomach hs = mgr.of(held.swallower());
        String id = victim.m_20148_().toString();
        hs.contents.removeIf(e -> PlayerHolding.isPlayerEntry(e) && id.equals(e.m_128461_("PlayerUuid")));
        HELD.remove(victim.m_20148_());
        PlayerHolding.restore(victim, held, holder);
        mgr.m_77762_();
        if (holder != null) {
            StomachLogic.say(holder, (Component)Component.m_237119_().m_7220_(victim.m_7755_()).m_130946_(" got out."));
            StomachLogic.sync(holder, hs);
            StomachLogic.sound(holder, GulpSound.SPIT);
        }
    }

    public static void tickHeld(ServerPlayer victim) {
        Held held = HELD.get(victim.m_20148_());
        if (held == null) {
            return;
        }
        ServerPlayer holder = victim.m_20194_().m_6846_().m_11259_(held.swallower());
        if (holder == null || !holder.m_6084_() || holder.m_5833_()) {
            PlayerHolding.escape(victim);
            return;
        }
        if (victim.m_8954_() != holder) {
            victim.m_9213_((Entity)holder);
        }
    }

    public static boolean tickEntry(ServerPlayer swallower, CompoundTag entry) {
        ServerPlayer victim;
        Held held;
        UUID id = PlayerHolding.victimId(entry);
        Held held2 = held = id == null ? null : HELD.get(id);
        if (held == null || !held.swallower().equals(swallower.m_20148_())) {
            return false;
        }
        int ticks = entry.m_128451_("HeldTicks") + 1;
        entry.m_128405_("HeldTicks", ticks);
        if (ticks > (Integer)GulpServerConfig.PLAYER_MAX_HOLD_SECONDS.get() * 20) {
            ServerPlayer victim2 = swallower.m_20194_().m_6846_().m_11259_(id);
            if (victim2 != null) {
                victim2.m_213846_((Component)Component.m_237113_((String)"You were let out."));
            }
            PlayerHolding.releaseEntry(swallower, entry);
            return false;
        }
        if (ticks % 20 == 0 && (victim = swallower.m_20194_().m_6846_().m_11259_(id)) != null) {
            entry.m_128469_("Entity").m_128350_("Health", victim.m_21223_());
        }
        return true;
    }

    public static void releaseAllHeldBy(ServerPlayer swallower) {
        StomachManager mgr = StomachManager.get(swallower.m_20194_());
        Stomach s = mgr.of(swallower.m_20148_());
        boolean any = s.contents.removeIf(e -> PlayerHolding.isPlayerEntry(e) && PlayerHolding.releaseEntry(swallower, e));
        if (any) {
            mgr.m_77762_();
            StomachLogic.sync(swallower, s);
        }
    }

    public static void onLogout(ServerPlayer p) {
        if (PlayerHolding.isHeld(p)) {
            PlayerHolding.escape(p);
        }
        PlayerHolding.releaseAllHeldBy(p);
    }

    public static void onLogin(ServerPlayer p) {
        StomachManager mgr = StomachManager.get(p.m_20194_());
        Stomach s = mgr.of(p.m_20148_());
        s.contents.removeIf(e -> {
            if (!PlayerHolding.isPlayerEntry(e)) {
                return false;
            }
            UUID id = PlayerHolding.victimId(e);
            return id == null || !HELD.containsKey(id);
        });
        if (!PlayerHolding.isHeld(p) && p.getPersistentData().m_128441_(KEY_PREV_MODE)) {
            PlayerHolding.restore(p, null, null);
        }
    }

    public static void onServerStopping(MinecraftServer server) {
        for (UUID id : new ArrayList<UUID>(HELD.keySet())) {
            ServerPlayer victim = server.m_6846_().m_11259_(id);
            if (victim == null) continue;
            PlayerHolding.escape(victim);
        }
        HELD.clear();
    }

    private static UUID victimId(CompoundTag entry) {
        try {
            return UUID.fromString(entry.m_128461_("PlayerUuid"));
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static void restore(ServerPlayer victim, Held held, ServerPlayer anchor) {
        int modeId = held != null ? held.previousMode() : victim.getPersistentData().m_128451_(KEY_PREV_MODE);
        GameType mode = GameType.m_46393_((int)modeId);
        if (mode == GameType.SPECTATOR) {
            mode = GameType.SURVIVAL;
        }
        victim.m_9213_((Entity)victim);
        victim.m_143403_(mode);
        victim.getPersistentData().m_128473_(KEY_PREV_MODE);
        if (anchor != null) {
            ServerLevel w = anchor.m_284548_();
            Vec3 fwd = anchor.m_20252_(1.0f).m_82490_(1.5);
            double x = anchor.m_20185_() + fwd.f_82479_;
            double y = anchor.m_20186_();
            double z = anchor.m_20189_() + fwd.f_82481_;
            AABB box = victim.m_20191_().m_82386_(x - victim.m_20185_(), y - victim.m_20186_(), z - victim.m_20189_());
            if (!w.m_45772_(box)) {
                x = anchor.m_20185_();
                y = anchor.m_20186_();
                z = anchor.m_20189_();
            }
            victim.m_8999_(w, x, y, z, anchor.m_146908_() + 180.0f, 0.0f);
        }
        victim.m_213846_((Component)Component.m_237113_((String)"You are out."));
    }

    private record Held(UUID swallower, int previousMode) {
    }
}

