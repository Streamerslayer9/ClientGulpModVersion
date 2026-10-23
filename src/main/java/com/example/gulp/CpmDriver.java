/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package com.example.gulp;

import com.example.gulp.ClientState;
import com.example.gulp.CpmBridge;
import com.example.gulp.GulpConfig;
import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;

public final class CpmDriver {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String[] BELLY_ALIASES = new String[]{"belly", "stomach", "stomach_scale", "belly_scale", "tummy"};
    private static final String[] MOBS_ALIASES = new String[]{"belly_mobs", "stomach_mobs"};
    private static final String[] ITEMS_ALIASES = new String[]{"belly_items", "stomach_items"};
    private static float shownMobs;
    private static float shownItems;
    private static boolean localModelHasBelly;
    private static int ticks;
    private static boolean reportedFlag;
    private static int offTicks;
    private static final Map<String, Integer> LAST_SENT;
    private static final Map<String, String> RESOLVED;

    private CpmDriver() {
    }

    public static boolean localModelHandlesBelly() {
        return localModelHasBelly;
    }

    public static void reset() {
        shownMobs = 0.0f;
        shownItems = 0.0f;
        localModelHasBelly = false;
        reportedFlag = false;
        ticks = 0;
        LAST_SENT.clear();
        RESOLVED.clear();
    }

    public static void tick() {
        if (!GulpConfig.cpmSliders || !CpmBridge.available()) {
            // CPM sliders are off (or CPM is missing): hand the belly back to the
            // built-in layer for THIS player only, and zero out any slider we drove
            // earlier so the CPM belly doesn't stay stuck on this player's model.
            if (!LAST_SENT.isEmpty()) {
                if (CpmBridge.available()) {
                    for (String name : LAST_SENT.keySet()) {
                        CpmBridge.play(name, 0);
                    }
                }
                LAST_SENT.clear();
                RESOLVED.clear();
                shownMobs = 0.0f;
                shownItems = 0.0f;
            }
            localModelHasBelly = false;
            CpmDriver.report(CpmDriver.styleOff(), ++offTicks % 200 == 0);
            return;
        }
        if (++ticks % 2 != 0) {
            return;
        }
        float cap = Math.max(1.0E-4f, ClientState.cap());
        float mobTarget = CpmDriver.clamp01(ClientState.used() / cap);
        float itemTarget = CpmDriver.clamp01(ClientState.itemFill());
        shownMobs = CpmDriver.ease(shownMobs, mobTarget);
        shownItems = CpmDriver.ease(shownItems, itemTarget);
        float total = CpmDriver.clamp01(shownMobs + 0.5f * shownItems);
        boolean resend = ticks % 200 == 0;
        boolean bellyExists = CpmDriver.push(GulpConfig.cpmBellyName, BELLY_ALIASES, total, resend);
        boolean mobsExists = CpmDriver.push(GulpConfig.cpmMobsName, MOBS_ALIASES, shownMobs, resend);
        boolean itemsExists = CpmDriver.push(GulpConfig.cpmItemsName, ITEMS_ALIASES, shownItems, resend);
        localModelHasBelly = bellyExists || mobsExists || itemsExists;
        CpmDriver.report(localModelHasBelly || CpmDriver.styleOff(), resend);
    }

    private static boolean styleOff() {
        return GulpConfig.bellyStyle == GulpConfig.BellyStyle.OFF;
    }

    /** Tell the server (and through it every other client) whether OUR CPM model draws its own belly. */
    private static void report(boolean flag, boolean force) {
        if (flag == reportedFlag && !force) {
            return;
        }
        try {
            if (net.minecraft.client.Minecraft.m_91087_().m_91403_() == null) {
                return;
            }
            Net.CHANNEL.sendToServer((Object)new CpmFlagPacket(new java.util.UUID(0L, 0L), flag));
            reportedFlag = flag;
        }
        catch (Throwable t) {
            // not connected yet; try again on the next tick
        }
    }

    private static boolean push(String configured, String[] aliases, float ratio, boolean force) {
        if (configured == null || configured.isBlank()) {
            return false;
        }
        String name = CpmDriver.resolve(configured, aliases);
        if (name == null) {
            return false;
        }
        int max = CpmBridge.maxValue(name);
        if (max < 0) {
            RESOLVED.remove(configured);
            LAST_SENT.remove(name);
            return false;
        }
        int value = Math.max(0, Math.min(max, Math.round(ratio * (float)max)));
        Integer last = LAST_SENT.get(name);
        if (force || last == null || last != value) {
            CpmBridge.play(name, value);
            LAST_SENT.put(name, value);
        }
        return true;
    }

    private static String resolve(String configured, String[] aliases) {
        String known = RESOLVED.get(configured);
        if (known != null && CpmBridge.maxValue(known) >= 0) {
            return known;
        }
        if (known == null && ticks % 20 != 0) {
            return null;
        }
        LinkedHashSet<String> candidates = new LinkedHashSet<String>(CpmDriver.variants(configured));
        for (String alias : aliases) {
            candidates.addAll(CpmDriver.variants(alias));
        }
        for (String candidate : candidates) {
            int max = CpmBridge.maxValue(candidate);
            if (max < 0) continue;
            if (!candidate.equals(known)) {
                LOGGER.info("Vore Mod: found a CPM belly slider named '{}' (max {}) for '{}'", new Object[]{candidate, max, configured});
            }
            RESOLVED.put(configured, candidate);
            return candidate;
        }
        if (known != null) {
            RESOLVED.remove(configured);
        }
        return null;
    }

    private static Set<String> variants(String name) {
        LinkedHashSet<String> out = new LinkedHashSet<String>();
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return out;
        }
        for (String base : new String[]{trimmed, trimmed.replace('_', ' ')}) {
            out.add(base);
            out.add(base.toLowerCase(Locale.ROOT));
            out.add(base.toUpperCase(Locale.ROOT));
            out.add(CpmDriver.capitalize(base));
            out.add(CpmDriver.titleCase(base));
        }
        return out;
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    private static String titleCase(String s) {
        StringBuilder sb = new StringBuilder();
        boolean startOfWord = true;
        for (char c : s.toCharArray()) {
            if (c == '_' || c == ' ') {
                startOfWord = true;
                sb.append(c);
                continue;
            }
            sb.append(startOfWord ? Character.toUpperCase(c) : Character.toLowerCase(c));
            startOfWord = false;
        }
        return sb.toString();
    }

    private static float ease(float shown, float target) {
        float next = shown + (target - shown) * 0.2f;
        return Math.abs(target - next) < 0.004f ? target : next;
    }

    private static float clamp01(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    static {
        LAST_SENT = new HashMap<String, Integer>();
        RESOLVED = new HashMap<String, String>();
    }
}

