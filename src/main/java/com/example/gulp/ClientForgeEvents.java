/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.example.gulp;

import com.example.gulp.ClientModEvents;
import com.example.gulp.ClientState;
import com.example.gulp.CpmDriver;
import com.example.gulp.Net;
import com.example.gulp.ScreenLoop;
import com.example.gulp.StomachScreen;
import com.example.gulp.TargetFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="gulp", value={Dist.CLIENT})
public final class ClientForgeEvents {
    private ClientForgeEvents() {
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientState.reset();
        CpmDriver.reset();
        ScreenLoop.stop();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91074_ == null) {
            return;
        }
        CpmDriver.tick();
        while (ClientModEvents.SWALLOW_KEY.m_90859_()) {
            Entity target = TargetFinder.find((Player)mc.f_91074_, 4.0);
            Net.send(0, target == null ? -1 : target.m_19879_());
        }
        while (ClientModEvents.MODE_KEY.m_90859_()) {
            Net.send(1);
        }
        while (ClientModEvents.RELEASE_KEY.m_90859_()) {
            Net.send(mc.f_91074_.m_6144_() ? 3 : 2);
        }
        while (ClientModEvents.SCREEN_KEY.m_90859_()) {
            if (mc.f_91080_ != null) continue;
            mc.m_91152_((Screen)new StomachScreen());
        }
    }
}

