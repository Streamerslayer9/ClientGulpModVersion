package com.example.gulp;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** What the client currently knows. Plain Java only (no client-only classes), so it's safe to load anywhere. */
public final class ClientState {
    private ClientState() { }

    /** One swallowed mob as the server describes it. */
    public record EntryInfo(int uid, Component name, String typeId, float size, float hp, float maxHp, float digest) { }

    private static boolean hard;
    private static boolean allowSwallowed;
    private static int predator;
    private static float itemFill;
    private static int digestProgress;
    private static int level = 1, xp, xpNeeded = 100, points;
    private static float used, cap = 1.75f;
    private static int[] ranks = new int[Perk.values().length];
    private static List<EntryInfo> entries = new ArrayList<>();

    public static void apply(SyncPacket m) {
        hard = m.hard;
        level = m.level;
        xp = m.xp;
        xpNeeded = m.xpNeeded;
        used = m.used;
        cap = m.cap;
        points = m.points;
        allowSwallowed = m.allowSwallowed;
        predator = m.predator;
        itemFill = m.itemFill;
        digestProgress = m.digestProgress;
        ranks = m.ranks;
        entries = m.entries;
    }

    /** Set by client setup; shows the level-up chat message if the player has it turned on. */
    public static java.util.function.BiConsumer<Integer, Integer> levelUpListener;

    public static void fireLevelUp(int level, int points) {
        if (levelUpListener != null) levelUpListener.accept(level, points);
    }

    /** Shows an action-bar message on this client (unless the player turned them off). Set by client setup. */
    public static java.util.function.Consumer<Component> messageListener;

    public static void fireMessage(Component text) {
        if (messageListener != null) messageListener.accept(text);
    }

    /** Plays a Gulp sound on this client. Set by client setup (see ClientSounds). */
    public interface SoundListener { void play(int type, double x, double y, double z); }

    public static SoundListener soundListener;

    public static void firePlaySound(int type, double x, double y, double z) {
        if (soundListener != null) soundListener.play(type, x, y, z);
    }

    /** Forget everything (called when you leave a world/server so nothing carries over to the next one). */
    public static void reset() {
        hard = false;
        level = 1;
        xp = 0;
        xpNeeded = Stomach.xpForNext(1);
        points = 0;
        allowSwallowed = false;
        predator = 0;
        itemFill = 0f;
        digestProgress = 0;
        used = 0f;
        cap = 1.75f;
        ranks = new int[Perk.values().length];
        entries = new ArrayList<>();
        BELLY_TARGET.clear();
        BELLY_SHOWN.clear();
        BELLY_TIME.clear();
        CPM_BELLY.clear();
    }

    public static boolean isHard() { return hard; }
    public static boolean allowSwallowed() { return allowSwallowed; }
    public static int predator() { return predator; }
    public static float itemFill() { return itemFill; }
    public static int digestProgress() { return digestProgress; }
    public static int level() { return level; }
    public static int xp() { return xp; }
    public static int xpNeeded() { return xpNeeded; }
    public static int points() { return points; }
    public static float used() { return used; }
    public static float cap() { return cap; }
    public static int rank(Perk p) { return ranks[p.ordinal()]; }
    public static List<EntryInfo> entries() { return Collections.unmodifiableList(entries); }

    /** Changes whenever the screens need to rebuild their buttons. */
    public static String signature() {
        StringBuilder sb = new StringBuilder();
        sb.append(hard).append('|').append(points).append('|').append(Arrays.toString(ranks)).append('|');
        for (EntryInfo e : entries) sb.append(e.uid()).append(',');
        return sb.toString();
    }

    // Belly fullness (0..1) for every player we can see, keyed by UUID.
    // Custom player models can read this too: ClientState.bellyAmount(uuid)
    private static final Map<UUID, Float> BELLY_TARGET = new HashMap<>();
    private static final Map<UUID, Float> BELLY_SHOWN = new HashMap<>();
    private static final Map<UUID, Long> BELLY_TIME = new HashMap<>();

    public static void setBelly(UUID id, float ratio) { BELLY_TARGET.put(id, ratio); }

    /** Players whose Custom Player Model draws its own belly: the built-in skin bulge is not drawn on them. */
    private static final java.util.Set<UUID> CPM_BELLY = new java.util.HashSet<>();

    public static void setCpmBelly(UUID id, boolean has) {
        if (has) CPM_BELLY.add(id); else CPM_BELLY.remove(id);
    }

    public static boolean hasCpmBelly(UUID id) { return CPM_BELLY.contains(id); }

    public static float bellyAmount(UUID id) { return BELLY_TARGET.getOrDefault(id, 0f); }

    /** Eases the displayed belly toward the real value so it grows/shrinks smoothly. */
    public static float smoothedBelly(UUID id) {
        float target = bellyAmount(id);
        float cur = BELLY_SHOWN.getOrDefault(id, 0f);
        // Ease toward the real value over about a third of a second, the same at any frame rate.
        long now = System.nanoTime();
        Long last = BELLY_TIME.put(id, now);
        float seconds = last == null ? 0.016f : Math.min(0.25f, (now - last) / 1.0e9f);
        cur += (target - cur) * (1f - (float) Math.exp(-seconds / 0.35f));
        if (Math.abs(target - cur) < 0.002f) cur = target;
        BELLY_SHOWN.put(id, cur);
        return cur;
    }
}
