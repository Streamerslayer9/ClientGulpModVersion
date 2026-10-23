/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.fml.loading.FMLPaths
 */
package com.example.gulp;

import com.example.gulp.GulpSound;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import net.minecraftforge.fml.loading.FMLPaths;

public class GulpConfig {
    public static BellyStyle bellyStyle = BellyStyle.STOMACH;
    public static boolean levelUpMessages = true;
    public static boolean actionMessages = true;
    public static boolean cpmSliders = true;
    public static String cpmBellyName = "belly";
    public static String cpmMobsName = "belly_mobs";
    public static String cpmItemsName = "belly_items";
    public static boolean legacyPerkMenu = false;
    public static boolean useCustomSounds = true;
    private static final boolean[] SOUND_ON = new boolean[GulpSound.values().length];
    private static final float[] SOUND_VOLUME;

    public static float volume(GulpSound s) {
        return SOUND_VOLUME[s.ordinal()];
    }

    public static void setVolume(GulpSound s, float v) {
        GulpConfig.SOUND_VOLUME[s.ordinal()] = Math.max(0.0f, Math.min(2.0f, v));
    }

    public static boolean isOn(GulpSound s) {
        return SOUND_ON[s.ordinal()];
    }

    public static void setOn(GulpSound s, boolean on) {
        GulpConfig.SOUND_ON[s.ordinal()] = on;
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("gulp-client.properties");
    }

    public static void load() {
        try {
            Path f = GulpConfig.file();
            if (!Files.exists(f, new LinkOption[0])) {
                return;
            }
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(f, new OpenOption[0]);){
                props.load(in);
            }
            bellyStyle = BellyStyle.valueOf(props.getProperty("bellyStyle", BellyStyle.STOMACH.name()));
            levelUpMessages = Boolean.parseBoolean(props.getProperty("levelUpMessages", "true"));
            actionMessages = Boolean.parseBoolean(props.getProperty("actionMessages", "true"));
            cpmSliders = Boolean.parseBoolean(props.getProperty("cpmSliders", "true"));
            cpmBellyName = props.getProperty("cpmBellyName", "belly").trim();
            cpmMobsName = props.getProperty("cpmMobsName", "belly_mobs").trim();
            cpmItemsName = props.getProperty("cpmItemsName", "belly_items").trim();
            legacyPerkMenu = Boolean.parseBoolean(props.getProperty("legacyPerkMenu", "false"));
            useCustomSounds = Boolean.parseBoolean(props.getProperty("customSounds", "true"));
            for (GulpSound s : GulpSound.values()) {
                GulpConfig.setOn(s, Boolean.parseBoolean(props.getProperty("sound." + s.name(), "true")));
                GulpConfig.setVolume(s, Float.parseFloat(props.getProperty("volume." + s.name(), "1.0")));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void save() {
        try {
            Properties props = new Properties();
            props.setProperty("bellyStyle", bellyStyle.name());
            props.setProperty("levelUpMessages", Boolean.toString(levelUpMessages));
            props.setProperty("actionMessages", Boolean.toString(actionMessages));
            props.setProperty("cpmSliders", Boolean.toString(cpmSliders));
            props.setProperty("cpmBellyName", cpmBellyName);
            props.setProperty("cpmMobsName", cpmMobsName);
            props.setProperty("cpmItemsName", cpmItemsName);
            props.setProperty("legacyPerkMenu", Boolean.toString(legacyPerkMenu));
            props.setProperty("customSounds", Boolean.toString(useCustomSounds));
            for (GulpSound s : GulpSound.values()) {
                props.setProperty("sound." + s.name(), Boolean.toString(GulpConfig.isOn(s)));
                props.setProperty("volume." + s.name(), Float.toString(GulpConfig.volume(s)));
            }
            try (OutputStream out = Files.newOutputStream(GulpConfig.file(), new OpenOption[0]);){
                props.store(out, "Gulp client settings");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    static {
        Arrays.fill(SOUND_ON, true);
        SOUND_VOLUME = new float[GulpSound.values().length];
        Arrays.fill(SOUND_VOLUME, 1.0f);
    }

    public static enum BellyStyle {
        CLASSIC("Classic (whole torso)"),
        STOMACH("Stomach only"),
        OFF("Off (my model has one)");

        public final String label;

        private BellyStyle(String label) {
            this.label = label;
        }

        public BellyStyle next() {
            BellyStyle[] v = BellyStyle.values();
            return v[(this.ordinal() + 1) % v.length];
        }
    }
}

