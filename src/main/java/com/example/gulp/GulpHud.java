package com.example.gulp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Placeholder HUD built from plain rectangles. Swap these for your pixel art textures later. */
public final class GulpHud {
    private GulpHud() { }

    /** Draws text lined up against the right edge so long numbers grow to the left instead of off the screen. */
    private static void right(GuiGraphics g, Minecraft mc, String text, int rightEdge, int y, int color) {
        g.drawString(mc.font, text, rightEdge - mc.font.width(text), y, color);
    }

    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        int w = 100;
        int rightEdge = screenWidth - 8;
        int x = rightEdge - w;
        int y = screenHeight - 34;

        if (ClientState.points() > 0) {
            right(g, mc, "Perk points: " + ClientState.points()
                    + " [" + ClientModEvents.SCREEN_KEY.getTranslatedKeyMessage().getString() + "]", rightEdge, y - 24, 0xFFE64A);
        }
        right(g, mc, "Stomach Lv." + ClientState.level() + (ClientState.isHard() ? "  [HARD]" : "  [SOFT]"), rightEdge, y - 12, 0xFFFFFF);

        g.fill(x - 1, y - 1, x + w + 1, y + 9, 0xFF000000);
        g.fill(x, y, x + w, y + 8, 0xFF333333);
        int fill = (int) (w * Math.min(1f, ClientState.used() / ClientState.cap()));
        g.fill(x, y, x + fill, y + 8, ClientState.isHard() ? 0xFFC0502E : 0xFF5DB85D);
        right(g, mc, Stomach.fmtVolume(ClientState.used()) + " / " + Stomach.fmtVolume(ClientState.cap())
                + "  (" + ClientState.entries().size() + " held)", rightEdge, y + 11, 0xDDDDDD);

        int xpFill = (int) (w * (ClientState.xp() / (float) Math.max(1, ClientState.xpNeeded())));
        g.fill(x, y + 22, x + w, y + 25, 0xFF222222);
        g.fill(x, y + 22, x + xpFill, y + 25, 0xFFE6C84A);
    }
}
