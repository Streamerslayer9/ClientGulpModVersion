/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 */
package com.example.gulp;

import com.example.gulp.ClientModEvents;
import com.example.gulp.ClientState;
import com.example.gulp.Stomach;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class GulpHud {
    private GulpHud() {
    }

    private static void right(GuiGraphics g, Minecraft mc, String text, int rightEdge, int y, int color) {
        g.m_280488_(mc.f_91062_, text, rightEdge - mc.f_91062_.m_92895_(text), y, color);
    }

    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91066_.f_92062_ || mc.f_91074_ == null) {
            return;
        }
        int w = 100;
        int rightEdge = screenWidth - 8;
        int x = rightEdge - w;
        int y = screenHeight - 34;
        if (ClientState.points() > 0) {
            GulpHud.right(g, mc, "Perk points: " + ClientState.points() + " [" + ClientModEvents.SCREEN_KEY.m_90863_().getString() + "]", rightEdge, y - 24, 16770634);
        }
        GulpHud.right(g, mc, "Stomach Lv." + ClientState.level() + (ClientState.isHard() ? "  [HARD]" : "  [SOFT]"), rightEdge, y - 12, 0xFFFFFF);
        g.m_280509_(x - 1, y - 1, x + w + 1, y + 9, -16777216);
        g.m_280509_(x, y, x + w, y + 8, -13421773);
        int fill = (int)((float)w * Math.min(1.0f, ClientState.used() / ClientState.cap()));
        g.m_280509_(x, y, x + fill, y + 8, ClientState.isHard() ? -4173778 : -10635171);
        GulpHud.right(g, mc, Stomach.fmtVolume(ClientState.used()) + " / " + Stomach.fmtVolume(ClientState.cap()) + "  (" + ClientState.entries().size() + " held)", rightEdge, y + 11, 0xDDDDDD);
        int xpFill = (int)((float)w * ((float)ClientState.xp() / (float)Math.max(1, ClientState.xpNeeded())));
        g.m_280509_(x, y + 22, x + w, y + 25, -14540254);
        g.m_280509_(x, y + 22, x + xpFill, y + 25, -1652662);
    }
}

