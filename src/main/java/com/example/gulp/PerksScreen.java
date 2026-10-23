package com.example.gulp;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** Spend perk points (1 per level-up). Icons are plain in-game items. */
public class PerksScreen extends Screen {
    private static final int W = 250;
    private static final int ROW_H = 30;
    private static final int HEADER = 24;
    private static final int FOOTER = 28;
    private static final int H = HEADER + Perk.values().length * ROW_H + FOOTER;

    private String lastSig = "";

    public PerksScreen() {
        super(Component.literal("Stomach Perks"));
    }

    private int left() { return (width - W) / 2; }
    private int top() { return (height - H) / 2; }

    /** Swap these for your own pixel art later. */
    static ItemStack iconFor(Perk perk) {
        return switch (perk) {
            case ROOMY -> new ItemStack(Items.BARREL);
            case HEALING -> new ItemStack(Items.GOLDEN_APPLE);
            case RICH -> new ItemStack(Items.EMERALD);
            case QUICK -> new ItemStack(Items.ENDER_EYE);
            case IRON -> new ItemStack(Items.IRON_CHESTPLATE);
            case GOURMET -> new ItemStack(Items.COOKED_BEEF);
            case HEARTY -> new ItemStack(Items.GOLDEN_CARROT);
            case UNIVERSAL -> new ItemStack(Items.COMPOSTER);
        };
    }

    @Override
    protected void init() {
        ScreenLoop.start();
        lastSig = ClientState.signature();
        int x = left();
        int y = top();
        Perk[] perks = Perk.values();
        for (int i = 0; i < perks.length; i++) {
            Perk perk = perks[i];
            int idx = i;
            int ry = y + HEADER + i * ROW_H;
            // Click = 1 rank, Shift+click = 5 ranks, Ctrl+click = as many as you can afford.
            Button buy = Button.builder(Component.literal("+"), btn -> {
                        int count = hasControlDown() ? 100 : hasShiftDown() ? 5 : 1;
                        Net.send(6, idx | (count << 8));
                    })
                    .bounds(x + W - 34, ry + 5, 26, 20).build();
            buy.active = ClientState.points() > 0 && ClientState.rank(perk) < perk.maxRank;
            addRenderableWidget(buy);
        }
        addRenderableWidget(Button.builder(Component.literal("New look"), btn -> {
            GulpConfig.legacyPerkMenu = false;
            GulpConfig.save();
            this.minecraft.setScreen(new PerkTreeScreen());
        }).bounds(x + 92, y + 2, 64, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"), btn -> this.minecraft.setScreen(new StomachScreen()))
                .bounds(x + 8, y + H - 28, 60, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), btn -> onClose())
                .bounds(x + W - 68, y + H - 28, 60, 20).build());
    }

    @Override
    public void tick() {
        if (!lastSig.equals(ClientState.signature())) {
            clearWidgets();
            init();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int x = left();
        int y = top();

        g.fill(x, y, x + W, y + H, 0xE6141414);
        g.fill(x, y, x + W, y + 1, 0xFF666666);
        g.fill(x, y + H - 1, x + W, y + H, 0xFF666666);
        g.fill(x, y, x + 1, y + H, 0xFF666666);
        g.fill(x + W - 1, y, x + W, y + H, 0xFF666666);

        g.drawString(font, "Stomach Perks", x + 8, y + 8, 0xFFFFFF, true);
        String pts = "Perk points: " + ClientState.points();
        g.drawString(font, pts, x + W - 8 - font.width(pts), y + 8,
                ClientState.points() > 0 ? 0xFFE64A : 0x999999, true);

        Perk[] perks = Perk.values();
        for (int i = 0; i < perks.length; i++) {
            Perk perk = perks[i];
            int ry = y + HEADER + i * ROW_H;
            g.fill(x + 6, ry + 1, x + W - 6, ry + ROW_H - 2, 0x40FFFFFF);
            g.renderItem(iconFor(perk), x + 12, ry + 7);

            g.drawString(font, perk.title, x + 36, ry + 2, 0xFFFFFF, true);
            String rank = ClientState.rank(perk) + "/" + perk.maxRank;
            g.drawString(font, rank, x + W - 44 - font.width(rank), ry + 2,
                    ClientState.rank(perk) >= perk.maxRank ? 0x55FF55 : 0xBBBBBB, true);

            List<FormattedCharSequence> lines = font.split(Component.literal(perk.description), W - 80);
            for (int l = 0; l < Math.min(2, lines.size()); l++) {
                g.drawString(font, lines.get(l), x + 36, ry + 12 + l * 9, 0x999999, false);
            }
        }

        String hint = "Shift: +5   Ctrl: max";
        g.drawString(font, hint, x + (W - font.width(hint)) / 2, y + H - 22, 0x777777, false);

        super.render(g, mouseX, mouseY, partialTick);
    }
}
