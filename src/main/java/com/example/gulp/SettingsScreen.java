/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package com.example.gulp;

import com.example.gulp.ClientState;
import com.example.gulp.CpmBridge;
import com.example.gulp.GulpConfig;
import com.example.gulp.Net;
import com.example.gulp.ScreenLoop;
import com.example.gulp.SoundsScreen;
import com.example.gulp.StomachScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SettingsScreen
extends Screen {
    private static final int W = 250;
    private static final int H = 224;
    private Button bellyButton;
    private Button levelUpButton;
    private Button actionButton;
    private Button allowButton;
    private Button perkMenuButton;
    private Button cpmButton;

    public SettingsScreen() {
        super((Component)Component.m_237113_((String)"Gulp Settings"));
    }

    private int left() {
        return (this.f_96543_ - 250) / 2;
    }

    private int top() {
        return (this.f_96544_ - 224) / 2;
    }

    private void refresh() {
        this.bellyButton.m_93666_((Component)Component.m_237113_((String)("Belly style: " + GulpConfig.bellyStyle.label)));
        this.levelUpButton.m_93666_((Component)Component.m_237113_((String)("Level-up messages: " + (GulpConfig.levelUpMessages ? "ON" : "OFF"))));
        this.actionButton.m_93666_((Component)Component.m_237113_((String)("Action messages: " + (GulpConfig.actionMessages ? "ON" : "OFF"))));
        this.cpmButton.m_93666_((Component)Component.m_237113_((String)("CPM belly sliders: " + (GulpConfig.cpmSliders ? "ON" : "OFF") + (CpmBridge.available() ? "" : " (CPM not found)"))));
        this.perkMenuButton.m_93666_((Component)Component.m_237113_((String)("Perk menu: " + (GulpConfig.legacyPerkMenu ? "Legacy" : "New look"))));
        this.allowButton.m_93666_((Component)Component.m_237113_((String)("Let players swallow me: " + (ClientState.allowSwallowed() ? "ON" : "OFF"))));
    }

    protected void m_7856_() {
        ScreenLoop.start();
        int x = this.left();
        int y = this.top();
        this.bellyButton = Button.m_253074_((Component)Component.m_237119_(), btn -> {
            GulpConfig.bellyStyle = GulpConfig.bellyStyle.next();
            GulpConfig.save();
            this.refresh();
        }).m_252987_(x + 15, y + 22, 220, 20).m_253136_();
        this.levelUpButton = Button.m_253074_((Component)Component.m_237119_(), btn -> {
            GulpConfig.levelUpMessages = !GulpConfig.levelUpMessages;
            GulpConfig.save();
            this.refresh();
        }).m_252987_(x + 15, y + 46, 220, 20).m_253136_();
        this.allowButton = Button.m_253074_((Component)Component.m_237119_(), btn -> Net.send(8)).m_252987_(x + 15, y + 118, 220, 20).m_253136_();
        this.actionButton = Button.m_253074_((Component)Component.m_237119_(), btn -> {
            GulpConfig.actionMessages = !GulpConfig.actionMessages;
            GulpConfig.save();
            this.refresh();
        }).m_252987_(x + 15, y + 70, 220, 20).m_253136_();
        this.perkMenuButton = Button.m_253074_((Component)Component.m_237119_(), btn -> {
            GulpConfig.legacyPerkMenu = !GulpConfig.legacyPerkMenu;
            GulpConfig.save();
            this.refresh();
        }).m_252987_(x + 15, y + 142, 220, 20).m_253136_();
        this.cpmButton = Button.m_253074_((Component)Component.m_237119_(), btn -> {
            GulpConfig.cpmSliders = !GulpConfig.cpmSliders;
            GulpConfig.save();
            this.refresh();
        }).m_252987_(x + 15, y + 166, 220, 20).m_253136_();
        this.m_142416_((GuiEventListener)this.bellyButton);
        this.m_142416_((GuiEventListener)this.cpmButton);
        this.m_142416_((GuiEventListener)this.actionButton);
        this.m_142416_((GuiEventListener)this.perkMenuButton);
        this.m_142416_((GuiEventListener)this.allowButton);
        this.m_142416_((GuiEventListener)this.levelUpButton);
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Sounds..."), btn -> this.f_96541_.m_91152_((Screen)new SoundsScreen())).m_252987_(x + 15, y + 94, 220, 20).m_253136_());
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Back"), btn -> this.f_96541_.m_91152_((Screen)new StomachScreen())).m_252987_(x + 8, y + 224 - 28, 60, 20).m_253136_());
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Close"), btn -> this.m_7379_()).m_252987_(x + 250 - 68, y + 224 - 28, 60, 20).m_253136_());
        this.refresh();
    }

    public void m_86600_() {
        this.refresh();
    }

    public boolean m_7043_() {
        return false;
    }

    public void m_88315_(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(g);
        int x = this.left();
        int y = this.top();
        g.m_280509_(x, y, x + 250, y + 224, -434891756);
        g.m_280509_(x, y, x + 250, y + 1, -10066330);
        g.m_280509_(x, y + 224 - 1, x + 250, y + 224, -10066330);
        g.m_280509_(x, y, x + 1, y + 224, -10066330);
        g.m_280509_(x + 250 - 1, y, x + 250, y + 224, -10066330);
        g.m_280056_(this.f_96547_, "Settings", x + 8, y + 8, 0xFFFFFF, true);
        super.m_88315_(g, mouseX, mouseY, partialTick);
    }
}

