/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelLayerLocation
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.EntityRenderersEvent$AddLayers
 *  net.minecraftforge.client.event.EntityRenderersEvent$RegisterLayerDefinitions
 *  net.minecraftforge.client.event.RegisterGuiOverlaysEvent
 *  net.minecraftforge.client.event.RegisterKeyMappingsEvent
 *  net.minecraftforge.client.settings.IKeyConflictContext
 *  net.minecraftforge.client.settings.KeyConflictContext
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 */
package com.example.gulp;

import com.example.gulp.BellyLayer;
import com.example.gulp.BellyModel;
import com.example.gulp.ClientSounds;
import com.example.gulp.ClientState;
import com.example.gulp.GulpConfig;
import com.example.gulp.GulpHud;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid="gulp", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class ClientModEvents {
    public static final ModelLayerLocation BELLY_LAYER = new ModelLayerLocation(new ResourceLocation("gulp", "belly"), "main");
    public static final KeyMapping SWALLOW_KEY = new KeyMapping("key.gulp.swallow", InputConstants.Type.KEYSYM, 86, "key.category.gulp");
    public static final KeyMapping MODE_KEY = new KeyMapping("key.gulp.mode", InputConstants.Type.KEYSYM, 66, "key.category.gulp");
    public static final KeyMapping RELEASE_KEY = new KeyMapping("key.gulp.release", InputConstants.Type.KEYSYM, 71, "key.category.gulp");
    public static final KeyMapping INVENTORY_SWALLOW_KEY = new KeyMapping("key.gulp.inventory_swallow", (IKeyConflictContext)KeyConflictContext.GUI, InputConstants.Type.KEYSYM, 86, "key.category.gulp");
    public static final KeyMapping SCREEN_KEY = new KeyMapping("key.gulp.screen", InputConstants.Type.KEYSYM, 78, "key.category.gulp");

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        GulpConfig.load();
        ClientState.soundListener = ClientSounds::playWorld;
        ClientState.messageListener = text -> {
            if (!GulpConfig.actionMessages) {
                return;
            }
            Minecraft mc = Minecraft.m_91087_();
            if (mc.f_91074_ != null) {
                mc.f_91074_.m_5661_(text, true);
            }
        };
        ClientState.levelUpListener = (level, points) -> {
            if (!GulpConfig.levelUpMessages) {
                return;
            }
            Minecraft mc = Minecraft.m_91087_();
            mc.f_91065_.m_93076_().m_93785_((Component)Component.m_237113_((String)("Stomach reached level " + level + "! You have " + points + " perk point" + (points == 1 ? "" : "s") + " to spend (open the stomach screen).")));
        };
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent e) {
        e.register(SWALLOW_KEY);
        e.register(MODE_KEY);
        e.register(RELEASE_KEY);
        e.register(SCREEN_KEY);
        e.register(INVENTORY_SWALLOW_KEY);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(BELLY_LAYER, BellyModel::createLayer);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers e) {
        for (String skin : e.getSkins()) {
            LivingEntityRenderer renderer = e.getSkin(skin);
            if (!(renderer instanceof PlayerRenderer)) continue;
            PlayerRenderer playerRenderer = (PlayerRenderer)renderer;
            playerRenderer.m_115326_((RenderLayer)new BellyLayer((RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>)playerRenderer, e.getEntityModels()));
        }
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("stomach", (gui, graphics, partialTick, width, height) -> GulpHud.render(graphics, width, height));
    }
}

