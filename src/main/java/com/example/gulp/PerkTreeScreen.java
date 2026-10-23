/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 */
package com.example.gulp;

import com.example.gulp.ClientState;
import com.example.gulp.GulpConfig;
import com.example.gulp.Net;
import com.example.gulp.Perk;
import com.example.gulp.PerksScreen;
import com.example.gulp.ScreenLoop;
import com.example.gulp.StomachItems;
import com.example.gulp.StomachScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class PerkTreeScreen
extends Screen {
    private static final int W = 320;
    private static final int H = 272;
    private static final int GROWTH = -10758496;
    private static final int LOOT = -10967297;
    private static final int SURVIVAL = -38037;
    private static final int GOLD = -11443;
    private static final int DIM = -13290175;
    private static final int NODE_R = 13;
    private static final int RING_R = 17;
    private static final int SEGMENTS = 48;
    private static final int RX = 104;
    private static final int RY = 60;
    private static final int ORBIT_RX = 138;
    private static final int ORBIT_RY = 84;
    private static final int CENTER_Y = 106;
    private static final int DETAIL_Y = 192;
    private static final int DETAIL_H = 52;
    private final List<Perk> order = new ArrayList<Perk>();
    private final int[] starX = new int[45];
    private final int[] starY = new int[45];

    public PerkTreeScreen() {
        super((Component)Component.m_237113_((String)"Stomach Perks"));
        this.order.add(Perk.ROOMY);
        this.order.add(Perk.GOURMET);
        this.order.add(Perk.QUICK);
        this.order.add(Perk.RICH);
        this.order.add(Perk.UNIVERSAL);
        this.order.add(Perk.HEARTY);
        this.order.add(Perk.IRON);
        this.order.add(Perk.HEALING);
        for (Perk p : Perk.values()) {
            if (this.order.contains((Object)p)) continue;
            this.order.add(p);
        }
        Random random = new Random(7L);
        for (int i = 0; i < this.starX.length; ++i) {
            this.starX[i] = 4 + random.nextInt(312);
            this.starY[i] = 24 + random.nextInt(220);
        }
    }

    private int left() {
        return (this.f_96543_ - 320) / 2;
    }

    private int top() {
        return (this.f_96544_ - 272) / 2;
    }

    private int centerX() {
        return this.left() + 160;
    }

    private int centerY() {
        return this.top() + 106;
    }

    private double angle(int i) {
        return -1.5707963267948966 + Math.PI * 2 * (double)i / (double)this.order.size();
    }

    private int nodeX(int i) {
        return this.centerX() + (int)Math.round(Math.cos(this.angle(i)) * 104.0);
    }

    private int nodeY(int i) {
        return this.centerY() + (int)Math.round(Math.sin(this.angle(i)) * 60.0);
    }

    private static int colorOf(Perk perk) {
        return switch (perk) {
            case Perk.HEALING, Perk.IRON, Perk.HEARTY, Perk.UNIVERSAL -> -38037;
            case Perk.RICH -> -10967297;
            default -> -10758496;
        };
    }

    private static void disc(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; ++dy) {
            int dx = (int)Math.round(Math.sqrt((double)r * (double)r - (double)dy * (double)dy));
            g.m_280509_(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private static void ring(GuiGraphics g, int cx, int cy, int radius, int segments, int lit, int litColor, int dimColor) {
        for (int i = 0; i < segments; ++i) {
            double a = -1.5707963267948966 + Math.PI * 2 * (double)i / (double)segments;
            int px = cx + (int)Math.round(Math.cos(a) * (double)radius);
            int py = cy + (int)Math.round(Math.sin(a) * (double)radius);
            g.m_280509_(px - 1, py - 1, px + 1, py + 1, i < lit ? litColor : dimColor);
        }
    }

    private static int brighten(int color) {
        int r = color >> 16 & 0xFF;
        int gr = color >> 8 & 0xFF;
        int b = color & 0xFF;
        r += (255 - r) * 35 / 100;
        gr += (255 - gr) * 35 / 100;
        b += (255 - b) * 35 / 100;
        return 0xFF000000 | r << 16 | gr << 8 | b;
    }

    private static String effect(Perk perk, int rank) {
        return switch (perk) {
            case Perk.ROOMY -> "+" + rank * 6 + "% capacity";
            case Perk.HEALING -> rank + "% max HP / 5s";
            case Perk.RICH -> "Looting " + (String)(rank % 2 == 0 ? String.valueOf(rank / 2) : rank / 2 + " or " + (rank / 2 + 1));
            case Perk.QUICK -> String.format("-%.2fs, -%.2fs up", (double)rank * 0.075, (double)rank * 0.15);
            case Perk.IRON -> "-" + rank * 5 + "% struggle damage";
            case Perk.GOURMET -> "+" + rank * 5 + "% digest XP";
            case Perk.HEARTY -> String.format("+%.1f HP per digest", (double)rank * 0.5);
            case Perk.UNIVERSAL -> String.format("+%.1f Hunger Bars per %d items", Float.valueOf(StomachItems.hungerBars(rank)), 32);
            default -> "";
        };
    }

    protected void m_7856_() {
        ScreenLoop.start();
        int x = this.left();
        int y = this.top();
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Legacy"), btn -> {
            GulpConfig.legacyPerkMenu = true;
            GulpConfig.save();
            this.f_96541_.m_91152_((Screen)new PerksScreen());
        }).m_252987_(x + 92, y + 2, 64, 20).m_253136_());
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Back"), btn -> this.f_96541_.m_91152_((Screen)new StomachScreen())).m_252987_(x + 8, y + 272 - 24, 56, 20).m_253136_());
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Close"), btn -> this.m_7379_()).m_252987_(x + 320 - 64, y + 272 - 24, 56, 20).m_253136_());
    }

    private int hoveredNode(double mouseX, double mouseY) {
        for (int i = 0; i < this.order.size(); ++i) {
            double dy;
            double dx = mouseX - (double)this.nodeX(i);
            if (!(dx * dx + (dy = mouseY - (double)this.nodeY(i)) * dy <= 361.0)) continue;
            return i;
        }
        return -1;
    }

    public boolean m_6375_(double mouseX, double mouseY, int button) {
        int i;
        if (button == 0 && (i = this.hoveredNode(mouseX, mouseY)) >= 0) {
            int count = PerkTreeScreen.m_96637_() ? 100 : (PerkTreeScreen.m_96638_() ? 5 : 1);
            Net.send(6, this.order.get(i).ordinal() | count << 8);
            return true;
        }
        return super.m_6375_(mouseX, mouseY, button);
    }

    public boolean m_7043_() {
        return false;
    }

    public void m_88315_(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int rank;
        int py;
        int px;
        double a;
        int k;
        this.m_280273_(g);
        int x = this.left();
        int y = this.top();
        int cx = this.centerX();
        int cy = this.centerY();
        int n = this.order.size();
        int hovered = this.hoveredNode(mouseX, mouseY);
        g.m_280509_(x, y, x + 320, y + 272, -434891752);
        g.m_280509_(x, y, x + 320, y + 1, -10066330);
        g.m_280509_(x, y + 272 - 1, x + 320, y + 272, -10066330);
        g.m_280509_(x, y, x + 1, y + 272, -10066330);
        g.m_280509_(x + 320 - 1, y, x + 320, y + 272, -10066330);
        for (int i = 0; i < this.starX.length; ++i) {
            g.m_280509_(x + this.starX[i], y + this.starY[i], x + this.starX[i] + 1, y + this.starY[i] + 1, 0x40FFFFFF);
        }
        for (k = 0; k < 120; ++k) {
            a = -1.5707963267948966 + Math.PI * 2 * (double)k / 120.0;
            px = cx + (int)Math.round(Math.cos(a) * 138.0);
            py = cy + (int)Math.round(Math.sin(a) * 84.0);
            int sector = (int)Math.floor((a + 1.5707963267948966) / (Math.PI * 2) * (double)n + 0.5) % n;
            g.m_280509_(px, py, px + 2, py + 2, PerkTreeScreen.colorOf(this.order.get(sector)) & 0xFFFFFF | 0x70000000);
        }
        for (k = 0; k < 60; ++k) {
            a = Math.PI * 2 * (double)k / 60.0;
            px = cx + (int)Math.round(Math.cos(a) * 40.0);
            py = cy + (int)Math.round(Math.sin(a) * 28.0);
            g.m_280509_(px, py, px + 1, py + 1, -13158590);
        }
        int points = ClientState.points();
        PerkTreeScreen.disc(g, cx, cy, 24, -14803416);
        PerkTreeScreen.ring(g, cx, cy, 25, 64, points > 0 ? 64 : 0, -11443, -13882312);
        PoseStack pose = g.m_280168_();
        pose.m_85836_();
        pose.m_252880_((float)cx, (float)(cy - 12), 0.0f);
        pose.m_85841_(2.0f, 2.0f, 1.0f);
        g.m_280137_(this.f_96547_, String.valueOf(points), 0, 0, points > 0 ? -11443 : -7697772);
        pose.m_85849_();
        g.m_280137_(this.f_96547_, points == 1 ? "point" : "points", cx, cy + 8, -5592406);
        for (int i = 0; i < n; ++i) {
            int max;
            Perk perk = this.order.get(i);
            int nx = this.nodeX(i);
            int ny = this.nodeY(i);
            rank = ClientState.rank(perk);
            boolean maxed = rank >= (max = perk.maxRank);
            boolean canBuy = points > 0 && !maxed;
            int color = maxed ? -11443 : PerkTreeScreen.colorOf(perk);
            boolean hot = i == hovered;
            PerkTreeScreen.disc(g, nx, ny, 13, hot ? -13816518 : -15000796);
            int lit = rank == 0 ? 0 : Math.max(1, Math.round((float)(48 * rank) / (float)max));
            PerkTreeScreen.ring(g, nx, ny, 17, 48, lit, hot ? PerkTreeScreen.brighten(color) : color, -13290175);
            g.m_280480_(PerksScreen.iconFor(perk), nx - 8, ny - 8);
            String label = rank + "/" + max;
            int tw = this.f_96547_.m_92895_(label);
            g.m_280509_(nx - tw / 2 - 2, ny + 17 - 4, nx + tw / 2 + 3, ny + 17 + 5, -15658728);
            g.m_280056_(this.f_96547_, label, nx - tw / 2, ny + 17 - 3, maxed ? -11443 : (rank > 0 ? -1 : -7829368), false);
            if (!canBuy) continue;
            int bx = nx + 13 - 2;
            int by = ny - 13 - 4;
            g.m_280509_(bx, by, bx + 8, by + 8, -13730510);
            g.m_280509_(bx + 2, by + 4, bx + 6, by + 5, -1);
            g.m_280509_(bx + 3, by + 2, bx + 4, by + 7, -1);
        }
        g.m_280056_(this.f_96547_, "Stomach Perks", x + 8, y + 8, -1, true);
        String hint = "Click +1   Shift +5   Ctrl max";
        g.m_280056_(this.f_96547_, hint, x + 320 - 8 - this.f_96547_.m_92895_(hint), y + 8, -8882046, false);
        String[] names = new String[]{"Growth", "Loot", "Survival"};
        int[] colors = new int[]{-10758496, -10967297, -38037};
        for (int i = 0; i < names.length; ++i) {
            g.m_280509_(x + 8, y + 27 + i * 10, x + 13, y + 32 + i * 10, colors[i]);
            g.m_280056_(this.f_96547_, names[i], x + 16, y + 25 + i * 10, -5592406, false);
        }
        g.m_280509_(x + 8, y + 192, x + 320 - 8, y + 192 + 52, -16119282);
        if (hovered >= 0) {
            Perk perk = this.order.get(hovered);
            rank = ClientState.rank(perk);
            g.m_280056_(this.f_96547_, perk.title + "  " + rank + "/" + perk.maxRank, x + 12, y + 192 + 3, rank >= perk.maxRank ? -11443 : PerkTreeScreen.colorOf(perk), false);
            List desc = this.f_96547_.m_92923_((FormattedText)Component.m_237113_((String)perk.description), 296);
            for (int l = 0; l < Math.min(2, desc.size()); ++l) {
                g.m_280649_(this.f_96547_, (FormattedCharSequence)desc.get(l), x + 12, y + 192 + 13 + l * 9, -6250336, false);
            }
            g.m_280056_(this.f_96547_, this.f_96547_.m_92834_("Current: " + PerkTreeScreen.effect(perk, rank), 296), x + 12, y + 192 + 32, -1, false);
            if (rank < perk.maxRank) {
                g.m_280056_(this.f_96547_, this.f_96547_.m_92834_("Next: " + PerkTreeScreen.effect(perk, rank + 1), 296), x + 12, y + 192 + 41, -6430552, false);
            } else {
                g.m_280056_(this.f_96547_, "Max rank", x + 12, y + 192 + 41, -11443, false);
            }
        } else {
            g.m_280137_(this.f_96547_, "Hover a perk to see what it does.", x + 160, y + 192 + 22, -8882046);
        }
        super.m_88315_(g, mouseX, mouseY, partialTick);
    }
}

