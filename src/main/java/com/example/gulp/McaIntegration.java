/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.Brain
 *  net.minecraft.world.entity.ai.memory.MemoryModuleType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.schedule.Activity
 *  net.minecraftforge.fml.ModList
 *  org.slf4j.Logger
 */
package com.example.gulp;

import com.example.gulp.GulpServerConfig;
import com.example.gulp.Stomach;
import com.example.gulp.StomachLogic;
import com.example.gulp.StomachManager;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

public final class McaIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int WITNESS_RANGE = 16;
    private static final int MAX_WITNESSES = 12;
    private static final int MAX_PREDATOR = 200;
    private static Boolean modLoaded;
    private static boolean broken;
    private static Class<?> villagerClass;
    private static Method getVillagerBrain;
    private static Method getMemoriesForPlayer;
    private static Method getHearts;
    private static Method rewardHearts;
    private static Method getPersonality;
    private static Method isGuard;
    private static final List<Scare> SCARED;
    private static final Map<UUID, Long> LAST_REFUSAL_MESSAGE;
    private static int problemsLogged;

    private McaIntegration() {
    }

    public static boolean active() {
        if (!((Boolean)GulpServerConfig.MCA_ENABLED.get()).booleanValue()) {
            return false;
        }
        if (modLoaded == null) {
            modLoaded = ModList.get().isLoaded("mca");
        }
        return modLoaded != false && !broken && McaIntegration.ensureReflection();
    }

    public static Component status() {
        if (!((Boolean)GulpServerConfig.MCA_ENABLED.get()).booleanValue()) {
            return Component.m_237113_((String)"[Gulp] MCA: reactions are turned off in the server config.").m_130940_(ChatFormatting.GRAY);
        }
        if (modLoaded == null) {
            modLoaded = ModList.get().isLoaded("mca");
        }
        if (!modLoaded.booleanValue()) {
            return Component.m_237113_((String)"[Gulp] MCA: not found (Minecraft Comes Alive isn't installed).").m_130940_(ChatFormatting.GRAY);
        }
        if (McaIntegration.active()) {
            return Component.m_237113_((String)"[Gulp] MCA connected: villager reactions are on.").m_130940_(ChatFormatting.GREEN);
        }
        return Component.m_237113_((String)"[Gulp] MCA found, but it didn't match what I expected, so reactions are off (see latest.log).").m_130940_(ChatFormatting.RED);
    }

    static Class<?> mcaClass(String name) throws ClassNotFoundException {
        try {
            return Class.forName(name);
        }
        catch (ClassNotFoundException notPlain) {
            return Class.forName("forge." + name);
        }
    }

    private static boolean ensureReflection() {
        if (villagerClass != null) {
            return true;
        }
        try {
            villagerClass = McaIntegration.mcaClass("net.mca.entity.VillagerEntityMCA");
            getVillagerBrain = villagerClass.getMethod("getVillagerBrain", new Class[0]);
            Class<?> brainClass = McaIntegration.mcaClass("net.mca.entity.ai.brain.VillagerBrain");
            getMemoriesForPlayer = McaIntegration.method(brainClass, "getMemoriesForPlayer", 1);
            rewardHearts = McaIntegration.method(brainClass, "rewardHearts", 2);
            getPersonality = McaIntegration.method(brainClass, "getPersonality", 0);
            getHearts = McaIntegration.method(McaIntegration.mcaClass("net.mca.entity.ai.Memories"), "getHearts", 0);
            try {
                isGuard = McaIntegration.method(villagerClass, "isGuard", 0);
            }
            catch (NoSuchMethodException ignored) {
                isGuard = null;
            }
            LOGGER.info("Vore Mod: MCA found, villager reactions are ready.");
            return true;
        }
        catch (Throwable t) {
            broken = true;
            villagerClass = null;
            LOGGER.warn("Vore Mod: MCA is installed but its code isn't what I expected, so the MCA reactions are switched off.", t);
            return false;
        }
    }

    private static void problem(String what, Throwable t) {
        if (problemsLogged++ < 5) {
            LOGGER.warn("Vore Mod: MCA reaction problem: {}", (Object)what, (Object)t);
        }
    }

    private static Method method(Class<?> c, String name, int parameters) throws NoSuchMethodException {
        for (Method m : c.getMethods()) {
            if (!m.getName().equals(name) || m.getParameterCount() != parameters) continue;
            return m;
        }
        throw new NoSuchMethodException(c.getName() + "." + name);
    }

    public static boolean isVillager(Entity e) {
        return e != null && villagerClass != null && villagerClass.isInstance(e);
    }

    private static Object brainOf(Entity villager) throws ReflectiveOperationException {
        return getVillagerBrain.invoke((Object)villager, new Object[0]);
    }

    private static int heartsWith(Entity villager, Player player) {
        try {
            Object memories = getMemoriesForPlayer.invoke(McaIntegration.brainOf(villager), player);
            return (Integer)getHearts.invoke(memories, new Object[0]);
        }
        catch (Throwable t) {
            return 0;
        }
    }

    private static String personalityOf(Entity villager) {
        try {
            String string;
            Object personality = getPersonality.invoke(McaIntegration.brainOf(villager), new Object[0]);
            if (personality instanceof Enum) {
                Enum e = (Enum)personality;
                string = e.name();
            } else {
                string = "";
            }
            return string;
        }
        catch (Throwable t) {
            McaIntegration.problem("couldn't read a villager's personality", t);
            return "";
        }
    }

    private static boolean guard(Entity villager) {
        try {
            return isGuard != null && (Boolean)isGuard.invoke((Object)villager, new Object[0]) != false;
        }
        catch (Throwable t) {
            return false;
        }
    }

    private static Attitude attitude(Entity villager) {
        return switch (McaIntegration.personalityOf(villager)) {
            case "PEPPY", "CONFIDENT", "ATHLETIC", "ODD" -> Attitude.FAN;
            case "SENSITIVE", "SHY", "GLOOMY", "GRUMPY" -> Attitude.FEARFUL;
            default -> Attitude.NEUTRAL;
        };
    }

    private static void reward(Entity villager, ServerPlayer player, int hearts) {
        int scaled = Math.round((float)hearts * ((Double)GulpServerConfig.MCA_REACTION_MULTIPLIER.get()).floatValue());
        if (scaled == 0) {
            return;
        }
        try {
            rewardHearts.invoke(McaIntegration.brainOf(villager), player, scaled);
            LOGGER.info("Vore Mod: villager {} hearts {} for {}", new Object[]{villager.m_7755_().getString(), (scaled > 0 ? "+" : "") + scaled, player.m_7755_().getString()});
        }
        catch (Throwable t) {
            McaIntegration.problem("couldn't change a villager's hearts", t);
        }
    }

    private static void addPredator(ServerPlayer p, Stomach s, int amount) {
        if (amount <= 0) {
            return;
        }
        int lock = (Integer)GulpServerConfig.MCA_PREDATOR_LOCK.get();
        int before = s.predator;
        s.predator = Math.min(200, s.predator + amount);
        if (before < lock && s.predator >= lock) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Villagers are afraid of you now."));
        }
    }

    private static void scare(Entity villager, ServerPlayer player) {
        LivingEntity le;
        block3: {
            block2: {
                if (!((Boolean)GulpServerConfig.MCA_PANIC.get()).booleanValue() || !(villager instanceof LivingEntity)) break block2;
                le = (LivingEntity)villager;
                if (!McaIntegration.guard(villager)) break block3;
            }
            return;
        }
        SCARED.add(new Scare(le, player.m_20148_(), le.m_9236_().m_46467_() + 160L));
    }

    private static void reactNearby(ServerPlayer p, Entity subject, boolean hard) {
        int count = 0;
        for (Entity w : p.m_9236_().m_6249_((Entity)p, p.m_20191_().m_82400_(16.0), e -> e != subject && McaIntegration.isVillager(e))) {
            if (++count > 12) break;
            Attitude a = McaIntegration.attitude(w);
            int hearts = hard ? (a == Attitude.FAN ? -3 : (a == Attitude.FEARFUL ? -15 : -10)) : (a == Attitude.FAN ? 5 : (a == Attitude.FEARFUL ? -8 : -2));
            McaIntegration.reward(w, p, hearts);
            if (!hard && a != Attitude.FEARFUL) continue;
            McaIntegration.scare(w, p);
        }
    }

    public static void onSwallow(ServerPlayer p, Stomach s, LivingEntity swallowed) {
        boolean on = McaIntegration.active();
        if (!on || !McaIntegration.isVillager((Entity)swallowed)) {
            if (swallowed.m_6095_().toString().contains("villager")) {
                LOGGER.info("Vore Mod: swallowed {} ({}) but no MCA reaction: MCA reactions {}, is MCA villager: {}", new Object[]{swallowed.m_7755_().getString(), swallowed.getClass().getName(), on ? "on" : "off (config off, MCA missing, or MCA didn't match)", McaIntegration.isVillager((Entity)swallowed)});
            }
            return;
        }
        boolean hard = s.hard && (Boolean)GulpServerConfig.HARD_MODE_ENABLED.get() != false;
        Attitude a = McaIntegration.attitude((Entity)swallowed);
        LOGGER.info("Vore Mod: MCA villager {} swallowed (personality {}, attitude {}, {} mode)", new Object[]{swallowed.m_7755_().getString(), McaIntegration.personalityOf((Entity)swallowed), a, hard ? "hard" : "soft"});
        int hearts = hard ? (a == Attitude.FAN ? -10 : (a == Attitude.FEARFUL ? -50 : -40)) : (a == Attitude.FAN ? 15 : (a == Attitude.FEARFUL ? -25 : -10));
        McaIntegration.reward((Entity)swallowed, p, hearts);
        int predator = hard ? (a == Attitude.FAN ? 8 : (a == Attitude.FEARFUL ? 20 : 15)) : (a == Attitude.FAN ? 0 : (a == Attitude.FEARFUL ? 5 : 3));
        McaIntegration.addPredator(p, s, predator);
        McaIntegration.reactNearby(p, (Entity)swallowed, hard);
    }

    public static void onRelease(ServerPlayer p, Stomach s, Entity released, int heldTicks) {
        if (!McaIntegration.active() || !McaIntegration.isVillager(released)) {
            return;
        }
        Attitude a = McaIntegration.attitude(released);
        if (a == Attitude.FAN) {
            McaIntegration.reward(released, p, Math.min(25, 5 + heldTicks / 200));
        } else if (!s.hard) {
            McaIntegration.reward(released, p, a == Attitude.FEARFUL ? 3 : 5);
        }
    }

    public static void onDigest(ServerPlayer p, Stomach s, LivingEntity digested) {
        if (!McaIntegration.active() || !McaIntegration.isVillager((Entity)digested)) {
            return;
        }
        Attitude a = McaIntegration.attitude((Entity)digested);
        McaIntegration.addPredator(p, s, a == Attitude.FAN ? 10 : (a == Attitude.FEARFUL ? 35 : 25));
        for (Entity w : p.m_9236_().m_6249_((Entity)p, p.m_20191_().m_82400_(16.0), e -> e != digested && McaIntegration.isVillager(e))) {
            McaIntegration.scare(w, p);
        }
    }

    public static Component refusal(ServerPlayer p, Entity target) {
        if (!(McaIntegration.active() && ((Boolean)GulpServerConfig.MCA_LOCKOUT_ENABLED.get()).booleanValue() && McaIntegration.isVillager(target))) {
            return null;
        }
        Stomach s = StomachManager.get(p.m_20194_()).of(p.m_20148_());
        if (s.predator <= 0) {
            return null;
        }
        if (s.predator >= (Integer)GulpServerConfig.MCA_PREDATOR_LOCK.get() && McaIntegration.attitude(target) != Attitude.FAN) {
            return Component.m_237119_().m_7220_(target.m_7755_()).m_130946_(" is too afraid of you to talk.");
        }
        if (McaIntegration.heartsWith(target, (Player)p) <= (Integer)GulpServerConfig.MCA_HEARTS_LOCK.get()) {
            return Component.m_237119_().m_7220_(target.m_7755_()).m_130946_(" wants nothing to do with you right now.");
        }
        return null;
    }

    public static boolean shouldTell(ServerPlayer p) {
        long now = System.currentTimeMillis();
        Long last = LAST_REFUSAL_MESSAGE.put(p.m_20148_(), now);
        return last == null || now - last > 1500L;
    }

    public static void tickScared(MinecraftServer server) {
        if (SCARED.isEmpty()) {
            return;
        }
        Iterator<Scare> it = SCARED.iterator();
        while (it.hasNext()) {
            Scare s = it.next();
            LivingEntity v = s.villager();
            if (v.m_213877_() || !v.m_6084_() || v.m_9236_().m_46467_() > s.until()) {
                it.remove();
                continue;
            }
            try {
                ServerPlayer p = server.m_6846_().m_11259_(s.player());
                Brain brain = v.m_6274_();
                if (p != null) {
                    brain.m_21882_(MemoryModuleType.f_26382_, p, 40L);
                }
                brain.m_21889_(Activity.f_37984_);
            }
            catch (Throwable t) {
                it.remove();
            }
        }
    }

    static {
        SCARED = new ArrayList<Scare>();
        LAST_REFUSAL_MESSAGE = new HashMap<UUID, Long>();
    }

    private static enum Attitude {
        FAN,
        NEUTRAL,
        FEARFUL;

    }

    private record Scare(LivingEntity villager, UUID player, long until) {
    }
}

