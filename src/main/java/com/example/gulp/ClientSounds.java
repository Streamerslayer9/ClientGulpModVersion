/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 */
package com.example.gulp;

import com.example.gulp.GulpConfig;
import com.example.gulp.GulpSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public final class ClientSounds {
    private ClientSounds() {
    }

    private static Choice choose(GulpSound sound) {
        if (!GulpConfig.isOn(sound)) {
            return null;
        }
        float percent = GulpConfig.volume(sound);
        if (percent <= 0.0f) {
            return null;
        }
        if (!GulpConfig.useCustomSounds) {
            SoundEvent vanilla = sound.vanilla();
            return vanilla == null ? null : new Choice(vanilla, Math.min(percent, 1.0f));
        }
        if (percent <= 1.0f) {
            return new Choice(sound.custom(), percent);
        }
        SoundEvent loud = sound.loud();
        return loud == null ? new Choice(sound.custom(), 1.0f) : new Choice(loud, percent / 2.0f);
    }

    public static void playWorld(int type, double x, double y, double z) {
        GulpSound sound = GulpSound.byIndex(type);
        if (sound == null) {
            return;
        }
        Choice c = ClientSounds.choose(sound);
        Minecraft mc = Minecraft.m_91087_();
        if (c == null || mc.f_91073_ == null) {
            return;
        }
        mc.f_91073_.m_7785_(x, y, z, c.event(), SoundSource.PLAYERS, c.volume(), 1.0f, false);
    }

    public static void preview(GulpSound sound) {
        if (sound == GulpSound.SCREEN_LOOP) {
            return;
        }
        Choice c = ClientSounds.choose(sound);
        if (c == null) {
            return;
        }
        Minecraft.m_91087_().m_91106_().m_120367_((SoundInstance)SimpleSoundInstance.m_119755_((SoundEvent)c.event(), (float)1.0f, (float)c.volume()));
    }

    private record Choice(SoundEvent event, float volume) {
    }
}

