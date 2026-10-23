package com.example.gulp;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Swallowing other players. Built to be safe in multiplayer:
 *  - a player can only be swallowed if THEY turned on "Let players swallow me" (off by default),
 *  - swallowed players are only held: never damaged, never digested,
 *  - they can leave at any time by pressing the Release key, and are let out automatically after a while,
 *    when the swallower dies, logs out, changes dimension, or the server stops,
 *  - server admins can switch the whole thing off (playerSwallowingEnabled in gulp-server.toml).
 *
 * How it works: the swallowed player is put in spectator mode looking through the swallower's eyes (so they
 * can't interact with anything), and their previous game mode is remembered, including in their saved data so a
 * crash can never leave anyone stuck in spectator mode.
 */
public final class PlayerHolding {
    private static final String KEY_PREV_MODE = "gulp_prev_mode";

    private record Held(UUID swallower, int previousMode) { }

    /** victim -> who is holding them. Only lives while the server runs. */
    private static final Map<UUID, Held> HELD = new HashMap<>();

    private PlayerHolding() { }

    public static boolean isHeld(ServerPlayer p) { return HELD.containsKey(p.getUUID()); }

    public static boolean isPlayerEntry(CompoundTag entry) { return entry.contains("PlayerUuid"); }

    // ------------------------------------------------------------------ swallowing

    /** @return true if the player was swallowed (the caller then adds the cooldown, sound and message) */
    public static boolean swallow(ServerPlayer p, Stomach s, ServerPlayer victim, StomachManager mgr) {
        if (!GulpServerConfig.PLAYER_SWALLOWING_ENABLED.get()) {
            StomachLogic.say(p, Component.literal("Swallowing players is turned off on this server."));
            return false;
        }
        if (!victim.isAlive() || victim.isSpectator() || isHeld(victim)) {
            StomachLogic.say(p, Component.literal("Nothing swallowable in reach."));
            return false;
        }
        if (!mgr.of(victim.getUUID()).allowSwallowed) {
            StomachLogic.say(p, Component.empty().append(victim.getName())
                    .append(" hasn't allowed being swallowed."));
            return false;
        }
        float size = Math.max(0.05f, victim.getBbWidth() * victim.getBbWidth() * victim.getBbHeight());
        if (size > s.capacity()) {
            StomachLogic.say(p, Component.literal("Too big for your stomach! (needs " + Stomach.fmtVolume(size)
                    + ", capacity " + Stomach.fmtVolume(s.capacity()) + ")"));
            return false;
        }
        if (s.used() + size > s.capacity()) {
            StomachLogic.say(p, Component.literal("Your stomach is too full."));
            return false;
        }
        if (s.contents.size() >= Stomach.MAX_MOBS) {
            StomachLogic.say(p, Component.literal("You can't hold more than " + Stomach.MAX_MOBS + " creatures at once."));
            return false;
        }

        // The entry looks like a mob entry so the stomach screen can list it, but it has no saved entity:
        // it points at the real player instead.
        CompoundTag entry = new CompoundTag();
        CompoundTag display = new CompoundTag();
        display.putString("id", "minecraft:player");
        display.putFloat("Health", victim.getHealth());
        entry.put("Entity", display);
        entry.putString("PlayerUuid", victim.getUUID().toString());
        entry.putFloat("Size", size);
        entry.putFloat("MaxHealth", victim.getMaxHealth());
        entry.putInt("Digest", 0);
        entry.putInt("DigestTime", 0);
        Component name = victim.getName();
        entry.putString("Name", name.getString());
        entry.putString("NameJson", Component.Serializer.toJson(name));
        entry.putInt("Uid", p.getRandom().nextInt());
        entry.putInt("HeldTicks", 0);
        s.contents.add(entry);

        int previous = victim.gameMode.getGameModeForPlayer().getId();
        HELD.put(victim.getUUID(), new Held(p.getUUID(), previous));
        victim.getPersistentData().putInt(KEY_PREV_MODE, previous); // survives a crash
        victim.setGameMode(GameType.SPECTATOR);
        victim.setCamera(p);
        victim.sendSystemMessage(Component.literal("You were swallowed by ").append(p.getName())
                .append(". Press your Release key (default G) to get out whenever you like."));
        return true;
    }

    // ------------------------------------------------------------------ letting them out

    /** Lets a swallowed player out of the swallower's stomach. The caller removes the entry from the list. */
    public static boolean releaseEntry(ServerPlayer swallower, CompoundTag entry) {
        UUID id = victimId(entry);
        if (id == null) return true;
        Held held = HELD.remove(id);
        ServerPlayer victim = swallower.getServer().getPlayerList().getPlayer(id);
        if (victim != null) restore(victim, held, swallower);
        return true;
    }

    /** The swallowed player pressed Release: they get out, whatever the swallower is doing. */
    public static void escape(ServerPlayer victim) {
        Held held = HELD.get(victim.getUUID());
        if (held == null) return;
        MinecraftServer server = victim.getServer();
        ServerPlayer holder = server.getPlayerList().getPlayer(held.swallower());
        StomachManager mgr = StomachManager.get(server);
        Stomach hs = mgr.of(held.swallower());
        String id = victim.getUUID().toString();
        hs.contents.removeIf(e -> isPlayerEntry(e) && id.equals(e.getString("PlayerUuid")));
        HELD.remove(victim.getUUID());
        restore(victim, held, holder);
        mgr.setDirty();
        if (holder != null) {
            StomachLogic.say(holder, Component.empty().append(victim.getName()).append(" got out."));
            StomachLogic.sync(holder, hs);
            StomachLogic.sound(holder, GulpSound.SPIT);
        }
    }

    /** Called every tick for every player: keeps a swallowed player inside, and lets them out if something went wrong. */
    public static void tickHeld(ServerPlayer victim) {
        Held held = HELD.get(victim.getUUID());
        if (held == null) return;
        ServerPlayer holder = victim.getServer().getPlayerList().getPlayer(held.swallower());
        if (holder == null || !holder.isAlive() || holder.isSpectator()) {
            escape(victim);
            return;
        }
        // Pressing sneak makes a spectator stop looking through someone else's eyes; put them straight back.
        if (victim.getCamera() != holder) victim.setCamera(holder);
    }

    /**
     * Called every tick for the swallower, for each swallowed player they hold.
     * @return false if this entry should be removed (the player is gone or their time ran out)
     */
    public static boolean tickEntry(ServerPlayer swallower, CompoundTag entry) {
        UUID id = victimId(entry);
        Held held = id == null ? null : HELD.get(id);
        if (held == null || !held.swallower().equals(swallower.getUUID())) return false; // stale entry
        int ticks = entry.getInt("HeldTicks") + 1;
        entry.putInt("HeldTicks", ticks);
        if (ticks > GulpServerConfig.PLAYER_MAX_HOLD_SECONDS.get() * 20) {
            ServerPlayer victim = swallower.getServer().getPlayerList().getPlayer(id);
            if (victim != null) {
                victim.sendSystemMessage(Component.literal("You were let out."));
            }
            releaseEntry(swallower, entry);
            return false;
        }
        if (ticks % 20 == 0) {
            ServerPlayer victim = swallower.getServer().getPlayerList().getPlayer(id);
            if (victim != null) entry.getCompound("Entity").putFloat("Health", victim.getHealth());
        }
        return true;
    }

    // ------------------------------------------------------------------ joining, leaving, dimension changes, shutdown

    /** Swallower logs out or changes dimension: let everyone they hold out first. */
    public static void releaseAllHeldBy(ServerPlayer swallower) {
        StomachManager mgr = StomachManager.get(swallower.getServer());
        Stomach s = mgr.of(swallower.getUUID());
        boolean any = s.contents.removeIf(e -> isPlayerEntry(e) && releaseEntry(swallower, e));
        if (any) {
            mgr.setDirty();
            StomachLogic.sync(swallower, s);
        }
    }

    /** A swallowed player logs out: put their game mode back BEFORE the game saves them. */
    public static void onLogout(ServerPlayer p) {
        if (isHeld(p)) escape(p);
        releaseAllHeldBy(p);
    }

    public static void onLogin(ServerPlayer p) {
        StomachManager mgr = StomachManager.get(p.getServer());
        Stomach s = mgr.of(p.getUUID());
        // Entries left over from a crash (nobody is really being held any more).
        s.contents.removeIf(e -> {
            if (!isPlayerEntry(e)) return false;
            UUID id = victimId(e);
            return id == null || !HELD.containsKey(id);
        });
        // A player who was swallowed when the server crashed would still be in spectator mode.
        if (!isHeld(p) && p.getPersistentData().contains(KEY_PREV_MODE)) restore(p, null, null);
    }

    public static void onServerStopping(MinecraftServer server) {
        for (UUID id : new ArrayList<>(HELD.keySet())) {
            ServerPlayer victim = server.getPlayerList().getPlayer(id);
            if (victim != null) escape(victim);
        }
        HELD.clear();
    }

    // ------------------------------------------------------------------ helpers

    private static UUID victimId(CompoundTag entry) {
        try {
            return UUID.fromString(entry.getString("PlayerUuid"));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /** Gives the player their game mode back and (if there's a swallower) puts them down in front of them. */
    private static void restore(ServerPlayer victim, Held held, ServerPlayer anchor) {
        int modeId = held != null ? held.previousMode() : victim.getPersistentData().getInt(KEY_PREV_MODE);
        GameType mode = GameType.byId(modeId);
        if (mode == GameType.SPECTATOR) mode = GameType.SURVIVAL; // never leave anyone stuck as a spectator
        victim.setCamera(victim);
        victim.setGameMode(mode);
        victim.getPersistentData().remove(KEY_PREV_MODE);
        if (anchor != null) {
            ServerLevel w = anchor.serverLevel();
            Vec3 fwd = anchor.getViewVector(1.0f).scale(1.5);
            double x = anchor.getX() + fwd.x;
            double y = anchor.getY();
            double z = anchor.getZ() + fwd.z;
            AABB box = victim.getBoundingBox().move(x - victim.getX(), y - victim.getY(), z - victim.getZ());
            if (!w.noCollision(box)) { // no room in front of them: use the spot they're standing on
                x = anchor.getX();
                y = anchor.getY();
                z = anchor.getZ();
            }
            victim.teleportTo(w, x, y, z, anchor.getYRot() + 180f, 0f);
        }
        victim.sendSystemMessage(Component.literal("You are out."));
    }
}
