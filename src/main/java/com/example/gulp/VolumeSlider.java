/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.AbstractSliderButton
 *  net.minecraft.network.chat.Component
 */
package com.example.gulp;

import com.example.gulp.ClientSounds;
import com.example.gulp.GulpConfig;
import com.example.gulp.GulpSound;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class VolumeSlider
extends AbstractSliderButton {
    private final GulpSound sound;

    public VolumeSlider(int x, int y, int width, int height, GulpSound sound) {
        super(x, y, width, height, (Component)Component.m_237119_(), (double)GulpConfig.volume(sound) / 2.0);
        this.sound = sound;
        this.m_5695_();
    }

    protected void m_5695_() {
        this.m_93666_((Component)Component.m_237113_((String)(this.sound.label + ": " + Math.round(GulpConfig.volume(this.sound) * 100.0f) + "%")));
    }

    protected void m_5697_() {
        this.f_93577_ = (double)Math.round(this.f_93577_ * 40.0) / 40.0;
        GulpConfig.setVolume(this.sound, (float)(this.f_93577_ * 2.0));
    }

    public void m_7691_(double mouseX, double mouseY) {
        super.m_7691_(mouseX, mouseY);
        GulpConfig.save();
        ClientSounds.preview(this.sound);
    }
}

