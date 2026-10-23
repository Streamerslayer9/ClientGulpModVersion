/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.ContainerHelper
 *  net.minecraft.world.item.ItemStack
 */
package com.example.gulp;

import com.example.gulp.Perk;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

public class Stomach {
    public static final int MAX_LEVEL = 99;
    public static final int MAX_MOBS = 100;
    public static final float SOFT_SECONDS_PER_LEVEL = 240.0f;
    public boolean hard = false;
    public int level = 1;
    public int xp = 0;
    public int points = 0;
    public final int[] ranks = new int[Perk.values().length];
    public final List<CompoundTag> contents = new ArrayList<CompoundTag>();
    public final NonNullList<ItemStack> storage = NonNullList.m_122780_((int)54, ItemStack.f_41583_);
    public boolean allowSwallowed = false;
    public int predator = 0;
    public int digestProgress = 0;
    public transient int cooldown = 0;

    public static String fmtVolume(double v) {
        return v >= 100.0 ? String.format("%.0f", v) : String.format("%.1f", v);
    }

    public static int xpForNext(int level) {
        if (level <= 10) {
            return (int)(Math.round(100.0 * Math.pow(level, 1.5) / 10.0) * 10L);
        }
        return 3160 + 40 * (level - 10);
    }

    public static int digestXp(float maxHealth) {
        return 20 + (int)(4.0f * maxHealth);
    }

    public int rank(Perk perk) {
        return this.ranks[perk.ordinal()];
    }

    public double capacity() {
        double base = 1.0 + 0.75 * (double)this.level + 0.08 * (double)this.level * (double)this.level;
        return base * (1.0 + 0.06 * (double)this.rank(Perk.ROOMY));
    }

    public int storageRows() {
        return Math.min(6, 1 + this.level / 17);
    }

    public int storageSlots() {
        return this.storageRows() * 9;
    }

    public ItemStack insertItem(ItemStack stack) {
        int i;
        ItemStack remaining = stack.m_41777_();
        int slots = this.storageSlots();
        for (i = 0; i < slots && !remaining.m_41619_(); ++i) {
            ItemStack in = (ItemStack)this.storage.get(i);
            if (in.m_41619_() || !ItemStack.m_150942_((ItemStack)in, (ItemStack)remaining) || in.m_41613_() >= in.m_41741_()) continue;
            int move = Math.min(remaining.m_41613_(), in.m_41741_() - in.m_41613_());
            in.m_41769_(move);
            remaining.m_41774_(move);
        }
        for (i = 0; i < slots && !remaining.m_41619_(); ++i) {
            if (!((ItemStack)this.storage.get(i)).m_41619_()) continue;
            this.storage.set(i, remaining.m_41620_(Math.min(remaining.m_41613_(), remaining.m_41741_())));
        }
        return remaining;
    }

    public float itemFill() {
        int slots = this.storageSlots();
        float total = 0.0f;
        for (int i = 0; i < slots; ++i) {
            ItemStack stack = (ItemStack)this.storage.get(i);
            if (stack.m_41619_()) continue;
            total += (float)stack.m_41613_() / (float)stack.m_41741_();
        }
        return Math.min(1.0f, total / (float)slots);
    }

    public double used() {
        double total = 0.0;
        for (CompoundTag e : this.contents) {
            total += (double)e.m_128457_("Size");
        }
        return total;
    }

    public static int cumulativePoints(int level) {
        return (int)((long)(Math.max(1, level) - 1) * (long)Perk.totalRanks() / 98L);
    }

    public int addXp(int amount) {
        if (this.level >= 99) {
            return 0;
        }
        this.xp += amount;
        int gained = 0;
        while (this.level < 99 && this.xp >= Stomach.xpForNext(this.level)) {
            this.xp -= Stomach.xpForNext(this.level);
            ++this.level;
            this.points += Stomach.cumulativePoints(this.level) - Stomach.cumulativePoints(this.level - 1);
            ++gained;
        }
        return gained;
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.m_128379_("Hard", this.hard);
        nbt.m_128405_("Level", this.level);
        nbt.m_128405_("Xp", this.xp);
        nbt.m_128405_("Points", this.points);
        CompoundTag perks = new CompoundTag();
        for (Perk p : Perk.values()) {
            perks.m_128405_(p.name(), this.ranks[p.ordinal()]);
        }
        nbt.m_128365_("Perks", (Tag)perks);
        nbt.m_128379_("AllowSwallowed", this.allowSwallowed);
        nbt.m_128405_("Predator", this.predator);
        nbt.m_128405_("DigestProgress", this.digestProgress);
        nbt.m_128365_("Storage", (Tag)ContainerHelper.m_18973_((CompoundTag)new CompoundTag(), this.storage));
        ListTag list = new ListTag();
        list.addAll(this.contents);
        nbt.m_128365_("Contents", (Tag)list);
        return nbt;
    }

    public static Stomach fromNbt(CompoundTag nbt) {
        Stomach s = new Stomach();
        s.hard = nbt.m_128471_("Hard");
        s.level = Math.max(1, nbt.m_128451_("Level"));
        s.xp = nbt.m_128451_("Xp");
        s.points = nbt.m_128441_("Points") ? nbt.m_128451_("Points") : s.level - 1;
        s.allowSwallowed = nbt.m_128471_("AllowSwallowed");
        s.predator = Math.max(0, nbt.m_128451_("Predator"));
        s.digestProgress = Math.max(0, Math.min(31, nbt.m_128451_("DigestProgress")));
        ContainerHelper.m_18980_((CompoundTag)nbt.m_128469_("Storage"), s.storage);
        CompoundTag perks = nbt.m_128469_("Perks");
        for (Perk p : Perk.values()) {
            s.ranks[p.ordinal()] = Math.min(p.maxRank, perks.m_128451_(p.name()));
        }
        int spent = 0;
        for (int r : s.ranks) {
            spent += r;
        }
        int shouldHave = Stomach.cumulativePoints(s.level);
        if (s.points + spent < shouldHave) {
            s.points = shouldHave - spent;
        }
        ListTag list = nbt.m_128437_("Contents", 10);
        for (int i = 0; i < list.size(); ++i) {
            s.contents.add(list.m_128728_(i));
        }
        return s;
    }
}

