/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Component$Serializer
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.Container
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.TamableAnimal
 *  net.minecraft.world.entity.animal.horse.AbstractHorse
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ChestMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.GameRules
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.items.ItemHandlerHelper
 *  net.minecraftforge.network.PacketDistributor
 *  org.slf4j.Logger
 */
package com.example.gulp;

import com.example.gulp.BellyPacket;
import com.example.gulp.GulpServerConfig;
import com.example.gulp.GulpSound;
import com.example.gulp.LevelUpPacket;
import com.example.gulp.McaIntegration;
import com.example.gulp.MessagePacket;
import com.example.gulp.Net;
import com.example.gulp.Perk;
import com.example.gulp.PlayerHolding;
import com.example.gulp.SoundPacket;
import com.example.gulp.Stomach;
import com.example.gulp.StomachItems;
import com.example.gulp.StomachManager;
import com.example.gulp.SyncPacket;
import com.example.gulp.TargetFinder;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;

public final class StomachLogic {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float STRUGGLE_CHANCE_PER_SECOND = 0.08f;
    private static final float DISSOLVE_SECONDS = 8.0f;
    private static final float STRUGGLE_BASE_DAMAGE = 2.0f;
    private static final float STRUGGLE_PER_VOLUME = 0.75f;
    private static final float STRUGGLE_MAX_DAMAGE = 5.0f;
    private static ServerPlayer digestingPlayer;
    private static LivingEntity digestingMob;
    private static int itemsGiven;
    private static int pendingLooting;
    private static final Map<UUID, SimpleContainer> OPEN_VIEWS;

    public static ServerPlayer digestingPlayer() {
        return digestingPlayer;
    }

    public static LivingEntity digestingMob() {
        return digestingMob;
    }

    public static void giveDrops(Collection<ItemEntity> drops) {
        if (digestingPlayer == null) {
            return;
        }
        for (ItemEntity drop : drops) {
            if (drop.m_32055_().m_41619_()) continue;
            itemsGiven += drop.m_32055_().m_41613_();
            ItemHandlerHelper.giveItemToPlayer((Player)digestingPlayer, (ItemStack)drop.m_32055_().m_41777_());
        }
    }

    public static int pendingLooting() {
        return pendingLooting;
    }

    private StomachLogic() {
    }

    public static void handleAction(ServerPlayer p, int action, int arg) {
        if (p == null || !p.m_6084_()) {
            return;
        }
        if (PlayerHolding.isHeld(p)) {
            if (action == 2 || action == 3) {
                PlayerHolding.escape(p);
            }
            return;
        }
        if (p.m_5833_()) {
            return;
        }
        StomachManager mgr = StomachManager.get(p.m_20194_());
        Stomach s = mgr.of(p.m_20148_());
        switch (action) {
            case 0: {
                StomachLogic.swallow(p, s, arg);
                break;
            }
            case 1: {
                if (!s.hard && !StomachLogic.hardModeAllowed()) {
                    StomachLogic.say(p, (Component)Component.m_237113_((String)"Hard mode is turned off on this server."));
                    break;
                }
                s.hard = !s.hard;
                StomachLogic.say(p, (Component)Component.m_237113_((String)(s.hard ? "Hard mode: contents will be digested for loot." : "Soft mode: contents are held safely.")));
                break;
            }
            case 2: {
                if (s.contents.isEmpty() || !StomachLogic.release(p, s.contents.get(s.contents.size() - 1))) break;
                s.contents.remove(s.contents.size() - 1);
                StomachLogic.sound(p, GulpSound.SPIT);
                break;
            }
            case 3: {
                boolean any = false;
                Iterator<CompoundTag> it = s.contents.iterator();
                while (it.hasNext()) {
                    if (!StomachLogic.release(p, it.next())) continue;
                    it.remove();
                    any = true;
                }
                if (!any) break;
                StomachLogic.sound(p, GulpSound.SPIT);
                break;
            }
            case 4: {
                int i = StomachLogic.indexOf(s, arg);
                if (i < 0 || !StomachLogic.release(p, s.contents.get(i))) break;
                s.contents.remove(i);
                StomachLogic.sound(p, GulpSound.SPIT);
                break;
            }
            case 5: {
                int i = StomachLogic.indexOf(s, arg);
                if (i < 0) break;
                if (PlayerHolding.isPlayerEntry(s.contents.get(i))) {
                    StomachLogic.say(p, (Component)Component.m_237113_((String)"Players are only held, never digested."));
                    break;
                }
                if (StomachLogic.hardModeAllowed()) {
                    StomachLogic.digest(p, s, s.contents.remove(i), true);
                    break;
                }
                StomachLogic.say(p, (Component)Component.m_237113_((String)"Digesting is turned off on this server."));
                break;
            }
            case 7: {
                StomachLogic.openStorage(p, mgr, s);
                break;
            }
            case 9: {
                StomachLogic.swallowFromInventorySlot(p, s, arg);
                break;
            }
            case 10: {
                ChestMenu cm;
                AbstractContainerMenu abstractContainerMenu;
                if (!((Boolean)GulpServerConfig.ITEM_STORAGE_ENABLED.get()).booleanValue() || !((abstractContainerMenu = p.f_36096_) instanceof ChestMenu) || (cm = (ChestMenu)abstractContainerMenu).m_39261_() != OPEN_VIEWS.get(p.m_20148_()) || !((abstractContainerMenu = cm.m_39261_()) instanceof SimpleContainer)) break;
                SimpleContainer view = (SimpleContainer)abstractContainerMenu;
                StomachItems.digestFirstSlot(p, s, view, arg);
                break;
            }
            case 8: {
                s.allowSwallowed = !s.allowSwallowed;
                StomachLogic.say(p, (Component)Component.m_237113_((String)(s.allowSwallowed ? "Other players can now swallow you (you can always get out with the Release key)." : "Other players can no longer swallow you.")));
                break;
            }
            case 6: {
                int bought;
                Perk perk = Perk.byIndex(arg & 0xFF);
                int count = Math.max(1, Math.min(100, arg >> 8));
                if (perk == null || (bought = Math.min(count, Math.min(s.points, perk.maxRank - s.rank(perk)))) <= 0) break;
                int n = perk.ordinal();
                s.ranks[n] = s.ranks[n] + bought;
                s.points -= bought;
                p.m_9236_().m_5594_(null, p.m_20183_(), SoundEvents.f_11871_, SoundSource.PLAYERS, 0.8f, 1.2f);
                break;
            }
        }
        mgr.m_77762_();
        StomachLogic.sync(p, s);
    }

    private static int indexOf(Stomach s, int uid) {
        for (int i = 0; i < s.contents.size(); ++i) {
            if (s.contents.get(i).m_128451_("Uid") != uid) continue;
            return i;
        }
        return -1;
    }

    private static void swallow(ServerPlayer p, Stomach s, int targetId) {
        AbstractHorse horse;
        TamableAnimal pet;
        Entity candidate;
        if (s.cooldown > 0) {
            return;
        }
        Entity target = null;
        if (targetId >= 0 && (candidate = p.m_284548_().m_6815_(targetId)) != null && TargetFinder.valid(candidate) && TargetFinder.withinReach((Player)p, candidate, 2.0)) {
            target = candidate;
        }
        if (target == null) {
            target = TargetFinder.find((Player)p, 4.0);
        }
        if (target instanceof ItemEntity) {
            ItemEntity item = (ItemEntity)target;
            StomachLogic.swallowItem(p, s, item);
            return;
        }
        if (target instanceof ServerPlayer) {
            ServerPlayer victim = (ServerPlayer)target;
            if (PlayerHolding.swallow(p, s, victim, StomachManager.get(p.m_20194_()))) {
                StomachLogic.finishSwallow(p, s, victim.m_7755_());
            }
            return;
        }
        if (!(target instanceof LivingEntity)) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Nothing swallowable in reach."));
            return;
        }
        LivingEntity le = (LivingEntity)target;
        if (le instanceof TamableAnimal && (pet = (TamableAnimal)le).m_21824_() && pet.m_21805_() != null && !pet.m_21805_().equals(p.m_20148_())) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"That pet belongs to someone else."));
            return;
        }
        if (le instanceof AbstractHorse && (horse = (AbstractHorse)le).m_30614_() && horse.m_21805_() != null && !horse.m_21805_().equals(p.m_20148_())) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"That animal belongs to someone else."));
            return;
        }
        float size = Math.max(0.05f, le.m_20205_() * le.m_20205_() * le.m_20206_());
        if ((double)size > s.capacity()) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)("Too big for your stomach! (needs " + Stomach.fmtVolume(size) + ", capacity " + Stomach.fmtVolume(s.capacity()) + ")")));
            return;
        }
        if (s.used() + (double)size > s.capacity()) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Your stomach is too full."));
            return;
        }
        if (s.contents.size() >= 100) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"You can't hold more than 100 creatures at once."));
            return;
        }
        le.m_8127_();
        McaIntegration.onSwallow(p, s, le);
        CompoundTag tag = new CompoundTag();
        if (!le.m_20223_(tag)) {
            return;
        }
        CompoundTag entry = new CompoundTag();
        entry.m_128365_("Entity", (Tag)tag);
        entry.m_128350_("Size", size);
        entry.m_128350_("MaxHealth", le.m_21233_());
        entry.m_128405_("Digest", 0);
        entry.m_128405_("DigestTime", 100 + (int)(150.0 * Math.sqrt(size)));
        Component displayName = le.m_7755_();
        entry.m_128359_("Name", displayName.getString());
        entry.m_128359_("NameJson", Component.Serializer.m_130703_((Component)displayName));
        entry.m_128405_("Uid", p.m_217043_().m_188502_());
        le.m_146870_();
        s.contents.add(entry);
        StomachLogic.finishSwallow(p, s, displayName);
    }

    private static void finishSwallow(ServerPlayer p, Stomach s, Component what) {
        int quick = s.rank(Perk.QUICK);
        float reduction = (p.m_146909_() < -30.0f ? 3.0f : 1.5f) * (float)quick;
        s.cooldown = Math.max(10, 40 - Math.round(reduction));
        StomachLogic.sound(p, GulpSound.SWALLOW);
        StomachLogic.say(p, (Component)Component.m_237113_((String)"Gulp! Swallowed ").m_7220_(what));
    }

    private static void swallowItem(ServerPlayer p, Stomach s, ItemEntity item) {
        ItemStack left;
        if (!((Boolean)GulpServerConfig.ITEM_STORAGE_ENABLED.get()).booleanValue()) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Item storage is turned off on this server."));
            return;
        }
        if (p.f_36096_ != p.f_36095_) {
            return;
        }
        ItemStack stack = item.m_32055_();
        int before = stack.m_41613_();
        int taken = before - (left = s.insertItem(stack)).m_41613_();
        if (taken <= 0) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Your item storage is full."));
            return;
        }
        if (left.m_41619_()) {
            item.m_146870_();
        } else {
            item.m_32045_(left);
        }
        StomachLogic.finishSwallow(p, s, (Component)Component.m_237113_((String)(taken + "x ")).m_7220_(stack.m_41786_()));
    }

    private static void swallowFromInventorySlot(ServerPlayer p, Stomach s, int arg) {
        boolean justOne;
        if (!((Boolean)GulpServerConfig.ITEM_STORAGE_ENABLED.get()).booleanValue()) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Item storage is turned off on this server."));
            return;
        }
        if (p.f_36096_ != p.f_36095_) {
            return;
        }
        int slotId = arg & 0xFFFF;
        boolean bl = justOne = (arg & 0x10000) != 0;
        if (slotId < 0 || slotId >= p.f_36095_.f_38839_.size()) {
            return;
        }
        Slot slot = p.f_36095_.m_38853_(slotId);
        if (!(slot.f_40218_ instanceof Inventory) || slot.getSlotIndex() >= 36 || !slot.m_6657_()) {
            return;
        }
        ItemStack stack = slot.m_7993_();
        int wanted = justOne ? 1 : stack.m_41613_();
        ItemStack offer = stack.m_41777_();
        offer.m_41764_(wanted);
        ItemStack left = s.insertItem(offer);
        int taken = wanted - left.m_41613_();
        if (taken <= 0) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Your item storage is full."));
            return;
        }
        Component name = stack.m_41786_();
        slot.m_6201_(taken);
        p.f_36096_.m_38946_();
        StomachLogic.sound(p, GulpSound.SWALLOW);
        StomachLogic.say(p, (Component)Component.m_237113_((String)("Gulp! Swallowed " + taken + "x ")).m_7220_(name));
    }

    private static void openStorage(ServerPlayer p, StomachManager mgr, Stomach s) {
        if (!((Boolean)GulpServerConfig.ITEM_STORAGE_ENABLED.get()).booleanValue()) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Item storage is turned off on this server."));
            return;
        }
        int rows = s.storageRows();
        int slots = rows * 9;
        SimpleContainer view = new SimpleContainer(slots);
        for (int i = 0; i < slots; ++i) {
            view.m_6836_(i, (ItemStack)s.storage.get(i));
        }
        OPEN_VIEWS.put(p.m_20148_(), view);
        view.m_19164_(c -> {
            for (int i = 0; i < slots; ++i) {
                s.storage.set(i, view.m_8020_(i));
            }
            mgr.m_77762_();
            StomachLogic.sync(p, s);
        });
        MenuType type = switch (rows) {
            case 1 -> MenuType.f_39957_;
            case 2 -> MenuType.f_39958_;
            case 3 -> MenuType.f_39959_;
            case 4 -> MenuType.f_39960_;
            case 5 -> MenuType.f_39961_;
            default -> MenuType.f_39962_;
        };
        p.m_5893_((MenuProvider)new SimpleMenuProvider((id, inv, player) -> new ChestMenu(type, id, inv, (Container)view, rows), (Component)Component.m_237113_((String)"Stomach Storage")));
    }

    public static void tickPlayer(StomachManager mgr, ServerPlayer p, Stomach s) {
        int syncEvery;
        if (s.cooldown > 0) {
            --s.cooldown;
        }
        if (s.predator > 0 && p.f_19797_ % ((Integer)GulpServerConfig.MCA_PREDATOR_DECAY_SECONDS.get() * 20) == 0) {
            --s.predator;
            StomachLogic.sync(p, s);
            mgr.m_77762_();
        }
        StomachItems.tick(p, mgr, s, s.hard && StomachLogic.hardModeAllowed());
        if (s.contents.isEmpty()) {
            return;
        }
        boolean changed = false;
        boolean dissolving = false;
        boolean hardNow = s.hard && StomachLogic.hardModeAllowed();
        int healAmount = s.rank(Perk.HEALING);
        Iterator<CompoundTag> it = s.contents.iterator();
        while (it.hasNext()) {
            CompoundTag e = it.next();
            if (PlayerHolding.isPlayerEntry(e)) {
                if (PlayerHolding.tickEntry(p, e)) continue;
                it.remove();
                changed = true;
                continue;
            }
            e.m_128405_("HeldTicks", e.m_128451_("HeldTicks") + 1);
            if (hardNow) {
                int digestTime;
                int d = e.m_128451_("Digest");
                if (d < (digestTime = e.m_128451_("DigestTime"))) {
                    e.m_128405_("Digest", ++d);
                }
                if (d >= digestTime) {
                    float health;
                    CompoundTag ent = e.m_128469_("Entity");
                    float maxHealth = Math.max(1.0f, e.m_128457_("MaxHealth"));
                    if (!e.m_128471_("Dissolving")) {
                        e.m_128379_("Dissolving", true);
                        if (ent.m_128457_("Health") <= 0.0f) {
                            ent.m_128350_("Health", maxHealth);
                        }
                    }
                    if ((health = ent.m_128457_("Health") - maxHealth / 160.0f) <= 0.0f) {
                        it.remove();
                        StomachLogic.digest(p, s, e, false);
                        changed = true;
                        continue;
                    }
                    ent.m_128350_("Health", health);
                    dissolving = true;
                }
                if (p.f_19797_ % 20 != 0 || !(p.m_21223_() > 4.0f) || !(p.m_217043_().m_188501_() < 0.08f)) continue;
                float strength = Math.min(5.0f, 2.0f + 0.75f * e.m_128457_("Size"));
                float damage = strength * Math.max(0.0f, 1.0f - 0.05f * (float)s.rank(Perk.IRON)) * ((Double)GulpServerConfig.STRUGGLE_DAMAGE_MULTIPLIER.get()).floatValue();
                if (!((damage = Math.min(damage, p.m_21223_() - 4.0f)) > 0.0f)) continue;
                p.m_6469_(p.m_9236_().m_269111_().m_269264_(), damage);
                StomachLogic.say(p, (Component)Component.m_237119_().m_7220_(StomachLogic.entryName(e)).m_130946_(" struggles inside you!"));
                continue;
            }
            if (healAmount <= 0 || p.f_19797_ % 100 != 0) continue;
            CompoundTag ent = e.m_128469_("Entity");
            float maxHealth = e.m_128457_("MaxHealth");
            float heal = Math.max(1.0f, maxHealth * 0.01f * (float)healAmount);
            ent.m_128350_("Health", Math.min(maxHealth, ent.m_128457_("Health") + heal));
        }
        if (!hardNow && p.f_19797_ % 20 == 0) {
            float fill = (float)Math.min(1.0, s.used() / s.capacity());
            StomachLogic.gainXp(p, s, fill * (float)Stomach.xpForNext(s.level) / 240.0f);
        }
        if (p.f_19797_ % 20 == 0) {
            mgr.m_77762_();
        }
        int n = s.contents.size() > 20 ? 20 : (syncEvery = dissolving ? 5 : 10);
        if (changed || p.f_19797_ % syncEvery == 0) {
            StomachLogic.sync(p, s);
        }
    }

    private static void digest(ServerPlayer p, Stomach s, CompoundTag entry, boolean manual) {
        int hearty;
        ServerLevel w = p.m_284548_();
        int lootRank = s.rank(Perk.RICH);
        int looting = lootRank / 2 + (lootRank % 2 == 1 && p.m_217043_().m_188501_() < 0.5f ? 1 : 0);
        EntityType.m_20642_((CompoundTag)entry.m_128469_("Entity"), (Level)w).ifPresent(e -> {
            e.m_20084_(UUID.randomUUID());
            e.m_7678_(p.m_20185_(), p.m_20186_(), p.m_20189_(), 0.0f, 0.0f);
            if (e instanceof LivingEntity) {
                LivingEntity le = (LivingEntity)e;
                McaIntegration.onDigest(p, s, le);
                le.m_20225_(true);
                digestingPlayer = p;
                digestingMob = le;
                pendingLooting = looting;
                itemsGiven = 0;
                try {
                    le.m_6469_(w.m_269111_().m_269075_((Player)p), Math.max(10000.0f, le.m_21233_() * 2.0f));
                    if (le.m_6084_()) {
                        le.m_6074_();
                    }
                }
                catch (RuntimeException ex) {
                    LOGGER.warn("Vore Mod: {} threw an error while being digested; letting it die normally instead.", (Object)le.m_6095_(), (Object)ex);
                    try {
                        w.m_7967_((Entity)le);
                        le.m_6074_();
                    }
                    catch (RuntimeException ex2) {
                        LOGGER.warn("Vore Mod: that mob also couldn't be killed normally.", (Throwable)ex2);
                    }
                }
                finally {
                    pendingLooting = 0;
                    digestingPlayer = null;
                    digestingMob = null;
                }
                if (le.m_6084_()) {
                    le.m_20225_(false);
                    w.m_7967_((Entity)le);
                }
            }
        });
        if (itemsGiven > 0) {
            p.m_9236_().m_5594_(null, p.m_20183_(), SoundEvents.f_12019_, SoundSource.PLAYERS, 0.25f, 1.3f);
            itemsGiven = 0;
        }
        if ((hearty = s.rank(Perk.HEARTY)) > 0) {
            p.m_5634_(0.5f * (float)hearty);
        }
        StomachLogic.sound(p, manual ? GulpSound.DIGEST_BUTTON : GulpSound.DIGEST);
        StomachLogic.gainXp(p, s, (float)Stomach.digestXp(entry.m_128457_("MaxHealth")) * (1.0f + 0.05f * (float)s.rank(Perk.GOURMET)));
    }

    public static boolean release(ServerPlayer p, CompoundTag entry) {
        if (PlayerHolding.isPlayerEntry(entry)) {
            return PlayerHolding.releaseEntry(p, entry);
        }
        ServerLevel w = p.m_284548_();
        Entity e = EntityType.m_20642_((CompoundTag)entry.m_128469_("Entity"), (Level)w).orElse(null);
        if (e == null) {
            return false;
        }
        Vec3 fwd = p.m_20252_(1.0f).m_82490_(1.5);
        float yaw = p.m_146908_() + 180.0f;
        e.m_7678_(p.m_20185_() + fwd.f_82479_, p.m_20186_() + 0.5, p.m_20189_() + fwd.f_82481_, yaw, 0.0f);
        if (!w.m_45786_(e)) {
            e.m_7678_(p.m_20185_(), p.m_20186_() + 0.1, p.m_20189_(), yaw, 0.0f);
        }
        e.m_20256_(Vec3.f_82478_);
        e.f_19789_ = 0.0f;
        boolean added = w.m_7967_(e);
        if (!added) {
            e.m_20084_(UUID.randomUUID());
            added = w.m_7967_(e);
        }
        if (added) {
            McaIntegration.onRelease(p, StomachManager.get(p.m_20194_()).of(p.m_20148_()), e, entry.m_128451_("HeldTicks"));
        }
        return added;
    }

    public static void releaseAll(ServerPlayer p) {
        StomachManager mgr = StomachManager.get(p.m_20194_());
        Stomach s = mgr.of(p.m_20148_());
        s.contents.removeIf(e -> StomachLogic.release(p, e));
        if (!p.m_9236_().m_46469_().m_46207_(GameRules.f_46133_)) {
            for (int i = 0; i < s.storage.size(); ++i) {
                ItemStack stack = (ItemStack)s.storage.get(i);
                if (stack.m_41619_()) continue;
                p.m_7197_(stack, true, false);
                s.storage.set(i, ItemStack.f_41583_);
            }
        }
        mgr.m_77762_();
        StomachLogic.sync(p, s);
    }

    private static void levelUp(ServerPlayer p, Stomach s) {
        p.m_9236_().m_5594_(null, p.m_20183_(), SoundEvents.f_12275_, SoundSource.PLAYERS, 0.7f, 1.0f);
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), (Object)new LevelUpPacket(s.level, s.points));
    }

    static void say(ServerPlayer p, Component text) {
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), (Object)new MessagePacket(text));
    }

    static void sound(ServerPlayer p, GulpSound type) {
        Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> p), (Object)new SoundPacket(type.ordinal(), p.m_20185_(), p.m_20186_(), p.m_20189_()));
    }

    public static Component entryName(CompoundTag entry) {
        MutableComponent parsed;
        if (entry.m_128441_("NameJson") && (parsed = Component.Serializer.m_130701_((String)entry.m_128461_("NameJson"))) != null) {
            return parsed;
        }
        return Component.m_237113_((String)entry.m_128461_("Name"));
    }

    private static boolean hardModeAllowed() {
        return (Boolean)GulpServerConfig.HARD_MODE_ENABLED.get();
    }

    static void gainXp(ServerPlayer p, Stomach s, float amount) {
        float scaled = amount * ((Double)GulpServerConfig.XP_MULTIPLIER.get()).floatValue();
        int whole = (int)scaled;
        if (p.m_217043_().m_188501_() < scaled - (float)whole) {
            ++whole;
        }
        if (whole > 0 && s.addXp(whole) > 0) {
            StomachLogic.levelUp(p, s);
        }
    }

    private static String fmt(double d) {
        return String.format("%.1f", d);
    }

    public static void sync(ServerPlayer p, Stomach s) {
        for (CompoundTag e : s.contents) {
            if (e.m_128441_("Uid")) continue;
            e.m_128405_("Uid", p.m_217043_().m_188502_());
        }
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), (Object)SyncPacket.from(s));
        float ratio = (float)Math.min(1.0, s.used() / s.capacity());
        Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> p), (Object)new BellyPacket(p.m_20148_(), ratio));
    }

    static {
        pendingLooting = 0;
        OPEN_VIEWS = new HashMap<UUID, SimpleContainer>();
    }
}

