/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.RegistryAccess$Frozen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.food.FoodProperties
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.CustomRecipe
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeManager
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.items.ItemHandlerHelper
 */
package com.example.gulp;

import com.example.gulp.GulpServerConfig;
import com.example.gulp.GulpSound;
import com.example.gulp.Perk;
import com.example.gulp.PlayerHolding;
import com.example.gulp.Stomach;
import com.example.gulp.StomachLogic;
import com.example.gulp.StomachManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.items.ItemHandlerHelper;

public final class StomachItems {
    public static final TagKey<Item> TRASH = ItemTags.create((ResourceLocation)new ResourceLocation("gulp", "stomach_trash"));
    public static final int DIGEST_THRESHOLD = 32;
    private static final int DECON_INTERVAL = 60;
    private static final int TRASH_PER_PASS = 4;
    private static final Map<Item, List<ItemStack>> PARTS = new HashMap<Item, List<ItemStack>>();
    private static RecipeManager cachedFor;
    private static final int XP_PER_ITEM = 2;

    public static int hungerPoints(int rank) {
        return 1 + Math.max(0, rank);
    }

    public static float hungerBars(int rank) {
        return (float)StomachItems.hungerPoints(rank) / 2.0f;
    }

    private StomachItems() {
    }

    public static void tick(ServerPlayer p, StomachManager mgr, Stomach s, boolean hardNow) {
        if (!((Boolean)GulpServerConfig.ITEM_STORAGE_ENABLED.get()).booleanValue()) {
            return;
        }
        if (!p.m_6084_() || p.m_5833_() || PlayerHolding.isHeld(p)) {
            return;
        }
        if (p.f_36096_ != p.f_36095_) {
            return;
        }
        boolean changed = false;
        if (hardNow && ((Boolean)GulpServerConfig.ITEM_DIGESTION_ENABLED.get()).booleanValue() && p.f_19797_ % 60 == 0) {
            changed |= StomachItems.breakDownOne(p, s);
        }
        if (changed) {
            mgr.m_77762_();
            StomachLogic.sync(p, s);
        }
    }

    public static void digestFirstSlot(ServerPlayer p, Stomach s, SimpleContainer view, int count) {
        List<ItemStack> parts;
        ItemStack stack = view.m_8020_(0);
        if (stack.m_41619_()) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"The first slot is empty."));
            return;
        }
        if (!((Boolean)GulpServerConfig.UNIVERSAL_DIGESTION_ENABLED.get()).booleanValue()) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Eating non-food items is turned off on this server."));
            return;
        }
        if (stack.m_41782_() && (stack.m_41783_().m_128441_("BlockEntityTag") || stack.m_41783_().m_128441_("Items"))) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Can't digest something that holds other items."));
            return;
        }
        if (stack.m_41793_() && !((Boolean)GulpServerConfig.DECONSTRUCT_ENCHANTED.get()).booleanValue()) {
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Enchanted items are protected on this server."));
            return;
        }
        if (stack.m_41763_() && (parts = StomachItems.partsOf(p.m_20194_(), stack.m_41720_())) != null) {
            StomachItems.breakDownFirstSlot(p, s, view, count, parts);
            return;
        }
        int n = Math.max(1, Math.min(Math.min(count, 64), stack.m_41613_()));
        Component name = stack.m_41786_();
        FoodProperties food = stack.getFoodProperties((LivingEntity)p);
        int nutrition = food == null ? 0 : food.m_38744_();
        view.m_7407_(0, n);
        int rank = s.rank(Perk.UNIVERSAL);
        s.digestProgress += n;
        int payouts = s.digestProgress / 32;
        s.digestProgress %= 32;
        int hunger = payouts * StomachItems.hungerPoints(rank);
        if (hunger > 0) {
            p.m_36324_().m_38707_(hunger, 0.0f);
        }
        float xp = n * (2 + nutrition);
        StomachLogic.gainXp(p, s, xp);
        MutableComponent msg = Component.m_237113_((String)("Ate " + n + "x ")).m_7220_(name);
        msg = hunger > 0 ? msg.m_6881_().m_7220_((Component)Component.m_237113_((String)String.format(" (+%.1f hunger)", Float.valueOf((float)hunger / 2.0f)))) : msg.m_6881_().m_7220_((Component)Component.m_237113_((String)(" (" + s.digestProgress + "/32)")));
        StomachLogic.say(p, (Component)msg);
        StomachLogic.sound(p, GulpSound.DIGEST_BUTTON);
        p.f_36096_.m_38946_();
    }

    private static void breakDownFirstSlot(ServerPlayer p, Stomach s, SimpleContainer view, int count, List<ItemStack> parts) {
        ItemStack stack = view.m_8020_(0);
        int n = Math.max(1, Math.min(Math.min(count, 64), stack.m_41613_()));
        Component name = stack.m_41786_();
        double condition = 1.0 - (double)stack.m_41773_() / (double)Math.max(1, stack.m_41776_());
        view.m_7407_(0, n);
        for (int i = 0; i < n; ++i) {
            for (ItemStack part : parts) {
                int amount = Math.max(1, (int)Math.round((double)part.m_41613_() * (0.5 + 0.5 * condition)));
                ItemStack left = view.m_19173_(new ItemStack((ItemLike)part.m_41720_(), amount));
                if (left.m_41619_()) continue;
                ItemHandlerHelper.giveItemToPlayer((Player)p, (ItemStack)left);
            }
        }
        StomachLogic.gainXp(p, s, n * 2);
        StomachLogic.say(p, (Component)Component.m_237113_((String)("Broke down " + n + "x ")).m_7220_(name));
        StomachLogic.sound(p, GulpSound.DIGEST_BUTTON);
        p.f_36096_.m_38946_();
    }

    private static boolean breakDownOne(ServerPlayer p, Stomach s) {
        ItemStack stack;
        int i;
        int slots = s.storageSlots();
        MinecraftServer server = p.m_20194_();
        for (i = 0; i < slots; ++i) {
            List<ItemStack> parts;
            stack = (ItemStack)s.storage.get(i);
            if (stack.m_41619_() || !stack.m_41763_() || stack.m_41793_() && !((Boolean)GulpServerConfig.DECONSTRUCT_ENCHANTED.get()).booleanValue() || (parts = StomachItems.partsOf(server, stack.m_41720_())) == null) continue;
            double condition = 1.0 - (double)stack.m_41773_() / (double)Math.max(1, stack.m_41776_());
            Component name = stack.m_41786_();
            stack.m_41774_(1);
            if (stack.m_41619_()) {
                s.storage.set(i, (Object)ItemStack.f_41583_);
            }
            for (ItemStack part : parts) {
                int count = Math.max(1, (int)Math.round((double)part.m_41613_() * (0.5 + 0.5 * condition)));
                StomachItems.stash(p, s, new ItemStack((ItemLike)part.m_41720_(), count));
            }
            StomachLogic.say(p, (Component)Component.m_237113_((String)"Broke down ").m_7220_(name));
            return true;
        }
        if (!((Boolean)GulpServerConfig.TRASH_DISSOLVE_ENABLED.get()).booleanValue()) {
            return false;
        }
        for (i = 0; i < slots; ++i) {
            stack = (ItemStack)s.storage.get(i);
            if (stack.m_41619_() || !stack.m_204117_(TRASH) || stack.m_41782_()) continue;
            stack.m_41774_(4);
            if (stack.m_41619_()) {
                s.storage.set(i, (Object)ItemStack.f_41583_);
            }
            return true;
        }
        return false;
    }

    private static void stash(ServerPlayer p, Stomach s, ItemStack stack) {
        ItemStack left = s.insertItem(stack);
        if (!left.m_41619_()) {
            ItemHandlerHelper.giveItemToPlayer((Player)p, (ItemStack)left);
        }
    }

    private static List<ItemStack> partsOf(MinecraftServer server, Item item) {
        RecipeManager rm = server.m_129894_();
        if (rm != cachedFor) {
            PARTS.clear();
            cachedFor = rm;
        }
        if (PARTS.containsKey(item)) {
            return PARTS.get(item);
        }
        ArrayList<ItemStack> result = null;
        RegistryAccess.Frozen access = server.m_206579_();
        for (CraftingRecipe recipe : rm.m_44013_(RecipeType.f_44107_)) {
            ItemStack out;
            if (recipe instanceof CustomRecipe || (out = recipe.m_8043_((RegistryAccess)access)).m_41619_() || out.m_41720_() != item) continue;
            LinkedHashMap<Item, Integer> counts = new LinkedHashMap<Item, Integer>();
            boolean selfReference = false;
            for (Ingredient ingredient : recipe.m_7527_()) {
                ItemStack[] options;
                if (ingredient.m_43947_() || (options = ingredient.m_43908_()).length == 0) continue;
                Item pick = options[0].m_41720_();
                if (pick == item) {
                    selfReference = true;
                }
                counts.merge(pick, 1, Integer::sum);
            }
            if (selfReference || counts.isEmpty()) continue;
            int perCraft = Math.max(1, out.m_41613_());
            ArrayList<ItemStack> parts = new ArrayList<ItemStack>();
            for (Map.Entry e : counts.entrySet()) {
                parts.add(new ItemStack((ItemLike)e.getKey(), Math.max(1, (Integer)e.getValue() / perCraft)));
            }
            result = parts;
            break;
        }
        PARTS.put(item, result);
        return result;
    }
}

