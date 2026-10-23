/*
 * Decompiled with CFR 0.152.
 */
package com.example.gulp;

public enum Perk {
    ROOMY("Roomy Stomach", "+6% stomach capacity per rank.", 30),
    HEALING("Healing Stomach", "Soft mode: held mobs heal 1% max HP/rank every 5s.", 15),
    RICH("Better Loot", "Looting on digested mobs: +1 level per 2 ranks (max V).", 10),
    QUICK("Quick Gulp", "Swallow cooldown -0.075s/rank, -0.15s looking up.", 10),
    IRON("Iron Stomach", "Struggling mobs hurt 5% less per rank.", 15),
    GOURMET("Gourmet", "+5% XP from digesting mobs per rank.", 20),
    HEARTY("Hearty Meal", "Heal 0.5 HP per rank whenever you digest a mob.", 10),
    UNIVERSAL("Iron Gut", "Digest any 32 items stored in your stomach to restore hunger.", 3);

    public final String title;
    public final String description;
    public final int maxRank;

    private Perk(String title, String description, int maxRank) {
        this.title = title;
        this.description = description;
        this.maxRank = maxRank;
    }

    public static int totalRanks() {
        int total = 0;
        for (Perk p : Perk.values()) {
            total += p.maxRank;
        }
        return total;
    }

    public static Perk byIndex(int i) {
        Perk[] v = Perk.values();
        return i >= 0 && i < v.length ? v[i] : null;
    }
}

