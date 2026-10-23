/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package com.example.gulp;

import com.example.gulp.ClientState;
import com.example.gulp.GulpConfig;
import com.example.gulp.Net;
import com.example.gulp.Perk;
import com.example.gulp.PerkTreeScreen;
import com.example.gulp.ScreenLoop;
import com.example.gulp.StomachScreen;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class PerksScreen
extends Screen {
    private static final int W = 250;
    private static final int ROW_H = 30;
    private static final int HEADER = 24;
    private static final int FOOTER = 28;
    private static final int H = 24 + Perk.values().length * 30 + 28;
    private String lastSig = "";

    public PerksScreen() {
        super((Component)Component.m_237113_((String)"Stomach Perks"));
    }

    private int left() {
        return (this.f_96543_ - 250) / 2;
    }

    private int top() {
        return (this.f_96544_ - H) / 2;
    }

    static ItemStack iconFor(Perk perk) {
        return switch (perk) {
            default -> throw new IncompatibleClassChangeError();
            case Perk.ROOMY -> new ItemStack((ItemLike)Items.f_42768_);
            case Perk.HEALING -> new ItemStack((ItemLike)Items.f_42436_);
            case Perk.RICH -> new ItemStack((ItemLike)Items.f_42616_);
            case Perk.QUICK -> new ItemStack((ItemLike)Items.f_42545_);
            case Perk.IRON -> new ItemStack((ItemLike)Items.f_42469_);
            case Perk.GOURMET -> new ItemStack((ItemLike)Items.f_42580_);
            case Perk.HEARTY -> new ItemStack((ItemLike)Items.f_42677_);
            case Perk.UNIVERSAL -> new ItemStack((ItemLike)Items.f_42726_);
        };
    }

    protected void m_7856_() {
        ScreenLoop.start();
        this.lastSig = ClientState.signature();
        int x = this.left();
        int y = this.top();
        Perk[] perks = Perk.values();
        for (int i = 0; i < perks.length; ++i) {
            Perk perk = perks[i];
            int idx = i;
            int ry = y + 24 + i * 30;
            Button buy = Button.m_253074_((Component)Component.m_237113_((String)"+"), btn -> {
                int count = PerksScreen.m_96637_() ? 100 : (PerksScreen.m_96638_() ? 5 : 1);
                Net.send(6, idx | count << 8);
            }).m_252987_(x + 250 - 34, ry + 5, 26, 20).m_253136_();
            buy.f_93623_ = ClientState.points() > 0 && ClientState.rank(perk) < perk.maxRank;
            this.m_142416_((GuiEventListener)buy);
        }
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"New look"), btn -> {
            GulpConfig.legacyPerkMenu = false;
            GulpConfig.save();
            this.f_96541_.m_91152_((Screen)new PerkTreeScreen());
        }).m_252987_(x + 92, y + 2, 64, 20).m_253136_());
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Back"), btn -> this.f_96541_.m_91152_((Screen)new StomachScreen())).m_252987_(x + 8, y + H - 28, 60, 20).m_253136_());
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Close"), btn -> this.m_7379_()).m_252987_(x + 250 - 68, y + H - 28, 60, 20).m_253136_());
    }

    public void m_86600_() {
        if (!this.lastSig.equals(ClientState.signature())) {
            this.m_169413_();
            this.m_7856_();
        }
    }

    public boolean m_7043_() {
        return false;
    }

    public void m_88315_(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(g);
        int x = this.left();
        int y = this.top();
        g.m_280509_(x, y, x + 250, y + H, -434891756);
        g.m_280509_(x, y, x + 250, y + 1, -10066330);
        g.m_280509_(x, y + H - 1, x + 250, y + H, -10066330);
        g.m_280509_(x, y, x + 1, y + H, -10066330);
        g.m_280509_(x + 250 - 1, y, x + 250, y + H, -10066330);
        g.m_280056_(this.f_96547_, "Stomach Perks", x + 8, y + 8, 0xFFFFFF, true);
        String pts = "Perk points: " + ClientState.points();
        g.m_280056_(this.f_96547_, pts, x + 250 - 8 - this.f_96547_.m_92895_(pts), y + 8, ClientState.points() > 0 ? 16770634 : 0x999999, true);
        Perk[] perks = Perk.values();
        for (int i = 0; i < perks.length; ++i) {
            Perk perk = perks[i];
            int ry = y + 24 + i * 30;
            g.m_280509_(x + 6, ry + 1, x + 250 - 6, ry + 30 - 2, 0x40FFFFFF);
            g.m_280480_(PerksScreen.iconFor(perk), x + 12, ry + 7);
            g.m_280056_(this.f_96547_, perk.title, x + 36, ry + 2, 0xFFFFFF, true);
            String rank = ClientState.rank(perk) + "/" + perk.maxRank;
            g.m_280056_(this.f_96547_, rank, x + 250 - 44 - this.f_96547_.m_92895_(rank), ry + 2, ClientState.rank(perk) >= perk.maxRank ? 0x55FF55 : 0xBBBBBB, true);
            List lines = this.f_96547_.m_92923_((FormattedText)Component.m_237113_((String)perk.description), 170);
            for (int l = 0; l < Math.min(2, lines.size()); ++l) {
                g.m_280649_(this.f_96547_, (FormattedCharSequence)lines.get(l), x + 36, ry + 12 + l * 9, 0x999999, false);
            }
        }
        String hint = "Shift: +5   Ctrl: max";
        g.m_280056_(this.f_96547_, hint, x + (250 - this.f_96547_.m_92895_(hint)) / 2, y + H - 22, 0x777777, false);
        super.m_88315_(g, mouseX, mouseY, partialTick);
    }
}

