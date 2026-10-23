/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingDropsEvent
 *  net.minecraftforge.event.entity.living.LivingExperienceDropEvent
 *  net.minecraftforge.event.entity.living.LootingLevelEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteractSpecific
 *  net.minecraftforge.event.server.ServerStoppingEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.LogicalSide
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.server.ServerLifecycleHooks
 */
package com.example.gulp;

import com.example.gulp.GulpServerConfig;
import com.example.gulp.McaIntegration;
import com.example.gulp.PlayerHolding;
import com.example.gulp.StomachLogic;
import com.example.gulp.StomachManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid="gulp")
public final class ServerEvents {
    private ServerEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side != LogicalSide.SERVER) {
            return;
        }
        Player player = e.player;
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer p = (ServerPlayer)player;
        PlayerHolding.tickHeld(p);
        StomachManager mgr = StomachManager.get(p.m_20194_());
        StomachLogic.tickPlayer(mgr, p, mgr.of(p.m_20148_()));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        Player player = e.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer p = (ServerPlayer)player;
        PlayerHolding.onLogin(p);
        StomachLogic.sync(p, StomachManager.get(p.m_20194_()).of(p.m_20148_()));
        if (((Boolean)GulpServerConfig.MCA_STATUS_MESSAGE.get()).booleanValue()) {
            p.m_213846_(McaIntegration.status());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        Player player = e.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)player;
            PlayerHolding.onLogout(p);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent e) {
        Player player = e.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)player;
            PlayerHolding.releaseAllHeldBy(p);
        }
    }

    @SubscribeEvent
    public static void onVillagerClick(PlayerInteractEvent.EntityInteract e) {
        ServerEvents.refuseIfAfraid((PlayerInteractEvent)e, e.getTarget());
    }

    @SubscribeEvent
    public static void onVillagerClickAt(PlayerInteractEvent.EntityInteractSpecific e) {
        ServerEvents.refuseIfAfraid((PlayerInteractEvent)e, e.getTarget());
    }

    private static void refuseIfAfraid(PlayerInteractEvent e, Entity target) {
        if (e.getLevel().m_5776_() || e.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = e.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer p = (ServerPlayer)player;
        Component refusal = McaIntegration.refusal(p, target);
        if (refusal == null) {
            return;
        }
        if (McaIntegration.shouldTell(p)) {
            StomachLogic.say(p, refusal);
        }
        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && server.m_129921_() % 10 == 0) {
            McaIntegration.tickScared(server);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent e) {
        PlayerHolding.onServerStopping(e.getServer());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        LivingEntity livingEntity = e.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)livingEntity;
            StomachLogic.releaseAll(p);
        }
    }

    @SubscribeEvent
    public static void onDigestDrops(LivingDropsEvent e) {
        if (StomachLogic.digestingPlayer() == null || e.getEntity() != StomachLogic.digestingMob()) {
            return;
        }
        StomachLogic.giveDrops(e.getDrops());
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onDigestXp(LivingExperienceDropEvent e) {
        ServerPlayer player = StomachLogic.digestingPlayer();
        if (player == null || e.getEntity() != StomachLogic.digestingMob()) {
            return;
        }
        if (e.getDroppedExperience() > 0) {
            player.m_6756_(e.getDroppedExperience());
        }
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLooting(LootingLevelEvent e) {
        int bonus = StomachLogic.pendingLooting();
        if (bonus > 0 && e.getDamageSource() != null && e.getDamageSource().m_7639_() instanceof ServerPlayer) {
            e.setLootingLevel(e.getLootingLevel() + bonus);
        }
    }
}

