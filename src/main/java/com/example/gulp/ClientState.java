/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.example.gulp;

import com.example.gulp.Perk;
import com.example.gulp.Stomach;
import com.example.gulp.SyncPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public final class ClientState {
    private static boolean hard;
    private static boolean allowSwallowed;
    private static int predator;
    private static float itemFill;
    private static int digestProgress;
    private static int level;
    private static int xp;
    private static int xpNeeded;
    private static int points;
    private static float used;
    private static float cap;
    private static int[] ranks;
    private static List<EntryInfo> entries;
    public static BiConsumer<Integer, Integer> levelUpListener;
    public static Consumer<Component> messageListener;
    public static SoundListener soundListener;
    private static final Map<UUID, Float> BELLY_TARGET;
    private static final Map<UUID, Float> BELLY_SHOWN;
    private static final Map<UUID, Long> BELLY_TIME;
    private static final java.util.Set<UUID> CPM_MODEL = new java.util.HashSet<UUID>();

    private ClientState() {
    }

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

    public static void fireLevelUp(int level, int points) {
        if (levelUpListener != null) {
            levelUpListener.accept(level, points);
        }
    }

    public static void fireMessage(Component text) {
        if (messageListener != null) {
            messageListener.accept(text);
        }
    }

    public static void firePlaySound(int type, double x, double y, double z) {
        if (soundListener != null) {
            soundListener.play(type, x, y, z);
        }
    }

    public static void reset() {
        hard = false;
        level = 1;
        xp = 0;
        xpNeeded = Stomach.xpForNext(1);
        points = 0;
        allowSwallowed = false;
        predator = 0;
        itemFill = 0.0f;
        digestProgress = 0;
        used = 0.0f;
        cap = 1.75f;
        ranks = new int[Perk.values().length];
        entries = new ArrayList<EntryInfo>();
        BELLY_TARGET.clear();
        BELLY_SHOWN.clear();
        BELLY_TIME.clear();
        CPM_MODEL.clear();
    }

    public static void setCpmModel(UUID id, boolean handlesBelly) {
        if (handlesBelly) {
            CPM_MODEL.add(id);
        } else {
            CPM_MODEL.remove(id);
        }
    }

    public static boolean hasCpmModel(UUID id) {
        return CPM_MODEL.contains(id);
    }

    public static boolean isHard() {
        return hard;
    }

    public static boolean allowSwallowed() {
        return allowSwallowed;
    }

    public static int predator() {
        return predator;
    }

    public static float itemFill() {
        return itemFill;
    }

    public static int digestProgress() {
        return digestProgress;
    }

    public static int level() {
        return level;
    }

    public static int xp() {
        return xp;
    }

    public static int xpNeeded() {
        return xpNeeded;
    }

    public static int points() {
        return points;
    }

    public static float used() {
        return used;
    }

    public static float cap() {
        return cap;
    }

    public static int rank(Perk p) {
        return ranks[p.ordinal()];
    }

    public static List<EntryInfo> entries() {
        return Collections.unmodifiableList(entries);
    }

    public static String signature() {
        StringBuilder sb = new StringBuilder();
        sb.append(hard).append('|').append(points).append('|').append(Arrays.toString(ranks)).append('|');
        for (EntryInfo e : entries) {
            sb.append(e.uid()).append(',');
        }
        return sb.toString();
    }

    public static void setBelly(UUID id, float ratio) {
        BELLY_TARGET.put(id, Float.valueOf(ratio));
    }

    public static float bellyAmount(UUID id) {
        return BELLY_TARGET.getOrDefault(id, Float.valueOf(0.0f)).floatValue();
    }

    public static float smoothedBelly(UUID id) {
        float target = ClientState.bellyAmount(id);
        float cur = BELLY_SHOWN.getOrDefault(id, Float.valueOf(0.0f)).floatValue();
        long now = System.nanoTime();
        Long last = BELLY_TIME.put(id, now);
        float seconds = last == null ? 0.016f : Math.min(0.25f, (float)(now - last) / 1.0E9f);
        if (Math.abs(target - (cur += (target - cur) * (1.0f - (float)Math.exp(-seconds / 0.35f)))) < 0.002f) {
            cur = target;
        }
        BELLY_SHOWN.put(id, Float.valueOf(cur));
        return cur;
    }

    static {
        level = 1;
        xpNeeded = 100;
        cap = 1.75f;
        ranks = new int[Perk.values().length];
        entries = new ArrayList<EntryInfo>();
        BELLY_TARGET = new HashMap<UUID, Float>();
        BELLY_SHOWN = new HashMap<UUID, Float>();
        BELLY_TIME = new HashMap<UUID, Long>();
    }

    public static interface SoundListener {
        public void play(int var1, double var2, double var4, double var6);
    }

    public record EntryInfo(int uid, Component name, String typeId, float size, float hp, float maxHp, float digest) {
    }
}

