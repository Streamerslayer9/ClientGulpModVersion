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

import com.example.gulp.GulpConfig;
import com.example.gulp.GulpSound;
import com.example.gulp.ScreenLoop;
import com.example.gulp.SettingsScreen;
import com.example.gulp.VolumeSlider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SoundsScreen
extends Screen {
    private static final int W = 250;
    private static final int H = 206;
    private Button setButton;
    private final Button[] toggles = new Button[GulpSound.values().length];
    private final VolumeSlider[] sliders = new VolumeSlider[GulpSound.values().length];

    public SoundsScreen() {
        super((Component)Component.m_237113_((String)"Gulp Sounds"));
    }

    private int left() {
        return (this.f_96543_ - 250) / 2;
    }

    private int top() {
        return (this.f_96544_ - 206) / 2;
    }

    private void refresh() {
        this.setButton.m_93666_((Component)Component.m_237113_((String)("Sounds: " + (GulpConfig.useCustomSounds ? "Custom" : "Minecraft"))));
        for (GulpSound sound : GulpSound.values()) {
            int i = sound.ordinal();
            boolean available = sound != GulpSound.SCREEN_LOOP || GulpConfig.useCustomSounds;
            this.toggles[i].m_93666_((Component)Component.m_237113_((String)(GulpConfig.isOn(sound) ? "ON" : "OFF")));
            this.toggles[i].f_93623_ = available;
            this.sliders[i].f_93623_ = available && GulpConfig.isOn(sound);
        }
    }

    protected void m_7856_() {
        ScreenLoop.start();
        int x = this.left();
        int y = this.top();
        this.setButton = Button.m_253074_((Component)Component.m_237119_(), btn -> {
            GulpConfig.useCustomSounds = !GulpConfig.useCustomSounds;
            GulpConfig.save();
            ScreenLoop.refresh();
            this.refresh();
        }).m_252987_(x + 15, y + 22, 220, 20).m_253136_();
        this.m_142416_((GuiEventListener)this.setButton);
        for (GulpSound sound : GulpSound.values()) {
            int i = sound.ordinal();
            int ry = y + 50 + i * 24;
            this.toggles[i] = Button.m_253074_((Component)Component.m_237119_(), btn -> {
                GulpConfig.setOn(sound, !GulpConfig.isOn(sound));
                GulpConfig.save();
                if (sound == GulpSound.SCREEN_LOOP) {
                    ScreenLoop.refresh();
                }
                this.refresh();
            }).m_252987_(x + 15, ry, 40, 20).m_253136_();
            this.sliders[i] = new VolumeSlider(x + 59, ry, 176, 20, sound);
            this.m_142416_((GuiEventListener)this.toggles[i]);
            this.m_142416_((GuiEventListener)this.sliders[i]);
        }
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Back"), btn -> this.f_96541_.m_91152_((Screen)new SettingsScreen())).m_252987_(x + 8, y + 206 - 28, 60, 20).m_253136_());
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Close"), btn -> this.m_7379_()).m_252987_(x + 250 - 68, y + 206 - 28, 60, 20).m_253136_());
        this.refresh();
    }

    public void m_7861_() {
        GulpConfig.save();
    }

    public boolean m_7043_() {
        return false;
    }

    public void m_88315_(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(g);
        int x = this.left();
        int y = this.top();
        g.m_280509_(x, y, x + 250, y + 206, -434891756);
        g.m_280509_(x, y, x + 250, y + 1, -10066330);
        g.m_280509_(x, y + 206 - 1, x + 250, y + 206, -10066330);
        g.m_280509_(x, y, x + 1, y + 206, -10066330);
        g.m_280509_(x + 250 - 1, y, x + 250, y + 206, -10066330);
        g.m_280056_(this.f_96547_, "Sounds", x + 8, y + 8, 0xFFFFFF, true);
        super.m_88315_(g, mouseX, mouseY, partialTick);
    }
}

