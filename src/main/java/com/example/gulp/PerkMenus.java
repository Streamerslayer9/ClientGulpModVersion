/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 */
package com.example.gulp;

import com.example.gulp.GulpConfig;
import com.example.gulp.PerkTreeScreen;
import com.example.gulp.PerksScreen;
import net.minecraft.client.gui.screens.Screen;

public final class PerkMenus {
    private PerkMenus() {
    }

    public static Screen create() {
        return GulpConfig.legacyPerkMenu ? new PerksScreen() : new PerkTreeScreen();
    }
}

