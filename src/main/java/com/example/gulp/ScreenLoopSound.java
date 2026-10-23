/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance$Attenuation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 */
package com.example.gulp;

import com.example.gulp.Gulp;
import com.example.gulp.GulpConfig;
import com.example.gulp.GulpSound;
import com.example.gulp.PerkTreeScreen;
import com.example.gulp.PerksScreen;
import com.example.gulp.SettingsScreen;
import com.example.gulp.SoundsScreen;
import com.example.gulp.StomachScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class ScreenLoopSound
extends AbstractTickableSoundInstance {
    private static final float BASE_VOLUME = 0.25f;

    public ScreenLoopSound() {
        super((SoundEvent)Gulp.SCREEN_LOOP.get(), SoundSource.PLAYERS, SoundInstance.m_235150_());
        this.f_119578_ = true;
        this.f_119579_ = 0;
        this.f_119573_ = ScreenLoopSound.currentVolume();
        this.f_119582_ = true;
        this.f_119580_ = SoundInstance.Attenuation.NONE;
    }

    private static float currentVolume() {
        return Math.min(1.0f, 0.25f * GulpConfig.volume(GulpSound.SCREEN_LOOP));
    }

    public boolean m_7784_() {
        return true;
    }

    public void m_7788_() {
        Screen screen = Minecraft.m_91087_().f_91080_;
        if (!(screen instanceof StomachScreen || screen instanceof PerksScreen || screen instanceof PerkTreeScreen || screen instanceof SettingsScreen || screen instanceof SoundsScreen)) {
            this.m_119609_();
            return;
        }
        this.f_119573_ = ScreenLoopSound.currentVolume();
    }
}

