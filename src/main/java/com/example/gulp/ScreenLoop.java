/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.resources.sounds.SoundInstance
 */
package com.example.gulp;

import com.example.gulp.GulpConfig;
import com.example.gulp.GulpSound;
import com.example.gulp.ScreenLoopSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;

public final class ScreenLoop {
    private static ScreenLoopSound current;

    private ScreenLoop() {
    }

    private static boolean enabled() {
        return GulpConfig.useCustomSounds && GulpConfig.isOn(GulpSound.SCREEN_LOOP);
    }

    public static void start() {
        if (!ScreenLoop.enabled()) {
            return;
        }
        if (current == null || current.m_7801_()) {
            current = new ScreenLoopSound();
            Minecraft.m_91087_().m_91106_().m_120367_((SoundInstance)current);
        }
    }

    public static void stop() {
        if (current != null) {
            Minecraft.m_91087_().m_91106_().m_120399_((SoundInstance)current);
            current = null;
        }
    }

    public static void refresh() {
        if (ScreenLoop.enabled()) {
            ScreenLoop.start();
        } else {
            ScreenLoop.stop();
        }
    }
}

