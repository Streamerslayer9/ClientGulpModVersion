/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraftforge.fml.ModList
 *  net.minecraftforge.fml.util.ObfuscationReflectionHelper
 *  org.slf4j.Logger
 */
package com.example.gulp;

import com.example.gulp.McaIntegration;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

public final class McaSkins {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean resolved;
    private static boolean ok;
    private static Method useVillagerRenderer;
    private static Method getVillager;
    private static Method getSkin;
    private static Method getColor;
    private static Method canUse;
    private static Class<?> skinLayerClass;
    private static Class<?> clothingLayerClass;
    private static final Map<Class<?>, Method> HAS_CUSTOM_SKIN;
    private static Field layersField;

    private McaSkins() {
    }

    private static boolean ready() {
        if (resolved) {
            return ok;
        }
        resolved = true;
        try {
            if (!ModList.get().isLoaded("mca")) {
                return false;
            }
            Class<?> client = McaIntegration.mcaClass("net.mca.MCAClient");
            useVillagerRenderer = client.getMethod("useVillagerRenderer", UUID.class);
            getVillager = McaIntegration.mcaClass("net.mca.client.model.CommonVillagerModel").getMethod("getVillager", Entity.class);
            skinLayerClass = McaIntegration.mcaClass("net.mca.client.render.layer.SkinLayer");
            clothingLayerClass = McaIntegration.mcaClass("net.mca.client.render.layer.ClothingLayer");
            Class<?> base = McaIntegration.mcaClass("net.mca.client.render.layer.VillagerLayer");
            getSkin = McaSkins.byName(base, "getSkin", 1);
            getColor = McaSkins.byName(base, "getColor", 2);
            canUse = McaSkins.byName(base, "canUse", 1);
            layersField = ObfuscationReflectionHelper.findField(LivingEntityRenderer.class, (String)"layers");
            ok = true;
        }
        catch (Throwable t) {
            ok = false;
            LOGGER.warn("Vore Mod: MCA is installed but its skin layers aren't what I expected, so the belly will use the normal skin ({}).", (Object)t.toString());
        }
        return ok;
    }

    private static Method byName(Class<?> c, String name, int parameters) throws NoSuchMethodException {
        for (Method m : c.getMethods()) {
            if (!m.getName().equals(name) || m.getParameterCount() != parameters) continue;
            return m;
        }
        throw new NoSuchMethodException(c.getName() + "." + name);
    }

    public static List<Object> findLayers(Object renderer) {
        ArrayList<Object> found = new ArrayList<Object>();
        if (!McaSkins.ready() || !(renderer instanceof LivingEntityRenderer)) {
            return found;
        }
        try {
            Object layers = layersField.get(renderer);
            if (layers instanceof List) {
                List list = (List)layers;
                for (Object layer : list) {
                    if (skinLayerClass.isInstance(layer)) {
                        found.add(0, layer);
                        continue;
                    }
                    if (!clothingLayerClass.isInstance(layer)) continue;
                    found.add(layer);
                }
            }
        }
        catch (Throwable t) {
            LOGGER.debug("Vore Mod: couldn't read the player renderer's layers: {}", (Object)t.toString());
        }
        return found;
    }

    public static List<Pass> passes(AbstractClientPlayer player, List<Object> mcaLayers, float partialTick) {
        if (mcaLayers == null || mcaLayers.isEmpty() || !McaSkins.ready()) {
            return null;
        }
        try {
            if (!((Boolean)useVillagerRenderer.invoke(null, player.m_20148_())).booleanValue()) {
                return null;
            }
            Object villager = getVillager.invoke(null, player);
            Method hasCustomSkin = HAS_CUSTOM_SKIN.computeIfAbsent(villager.getClass(), c -> {
                try {
                    return McaSkins.byName(c, "hasCustomSkin", 0);
                }
                catch (NoSuchMethodException e) {
                    return null;
                }
            });
            if (hasCustomSkin != null && ((Boolean)hasCustomSkin.invoke(villager, new Object[0])).booleanValue()) {
                return null;
            }
            ArrayList<Pass> passes = new ArrayList<Pass>();
            for (Object layer : mcaLayers) {
                ResourceLocation location;
                Object texture = getSkin.invoke(layer, player);
                if (!(texture instanceof ResourceLocation) || !((Boolean)canUse.invoke(layer, location = (ResourceLocation)texture)).booleanValue()) continue;
                float[] color = (float[])getColor.invoke(layer, player, Float.valueOf(partialTick));
                passes.add(new Pass(location, color[0], color[1], color[2], clothingLayerClass.isInstance(layer)));
            }
            return passes;
        }
        catch (Throwable t) {
            return null;
        }
    }

    static {
        HAS_CUSTOM_SKIN = new HashMap();
    }

    public record Pass(ResourceLocation texture, float r, float g, float b, boolean clothing) {
    }
}

