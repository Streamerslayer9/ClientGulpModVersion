/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.SpawnEggItem
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.registries.ForgeRegistries
 */
package com.example.gulp;

import com.example.gulp.ClientState;
import com.example.gulp.Net;
import com.example.gulp.PerkMenus;
import com.example.gulp.ScreenLoop;
import com.example.gulp.SettingsScreen;
import com.example.gulp.Stomach;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

public class StomachScreen
extends Screen {
    private static final int W = 250;
    private static final int ROWS = 5;
    private static final int ROW_H = 30;
    private static final int HEADER = 40;
    private static final int FOOTER = 56;
    private static final int H = 246;
    private static final Map<String, ItemStack> ICONS = new HashMap<String, ItemStack>();
    private static Map<EntityType<?>, SpawnEggItem> allEggs;
    private int page = 0;
    private String lastSig = "";

    public StomachScreen() {
        super((Component)Component.m_237113_((String)"Stomach"));
    }

    private int left() {
        return (this.f_96543_ - 250) / 2;
    }

    private int top() {
        return (this.f_96544_ - 246) / 2;
    }

    private static SpawnEggItem eggFor(EntityType<?> type) {
        SpawnEggItem egg = SpawnEggItem.m_43213_(type);
        if (egg != null) {
            return egg;
        }
        if (allEggs == null) {
            allEggs = new HashMap();
            for (Item item : ForgeRegistries.ITEMS) {
                if (!(item instanceof SpawnEggItem)) continue;
                SpawnEggItem e = (SpawnEggItem)item;
                try {
                    EntityType t = e.m_43228_(null);
                    if (t == null) continue;
                    allEggs.putIfAbsent(t, e);
                }
                catch (Exception exception) {}
            }
        }
        return allEggs.get(type);
    }

    private static ItemStack iconFor(String typeId) {
        return ICONS.computeIfAbsent(typeId, id -> {
            SpawnEggItem egg;
            if ("minecraft:player".equals(id)) {
                return new ItemStack((ItemLike)Items.f_42680_);
            }
            Optional type = EntityType.m_20632_((String)id);
            if (type.isPresent() && (egg = StomachScreen.eggFor((EntityType)type.get())) != null) {
                return new ItemStack((ItemLike)egg);
            }
            return ItemStack.f_41583_;
        });
    }

    private static String displayName(ClientState.EntryInfo e) {
        String name = e.name().getString();
        if (!name.isEmpty() && !name.startsWith("entity.")) {
            return name;
        }
        String id = e.typeId();
        String path = id.substring(id.indexOf(58) + 1);
        StringBuilder sb = new StringBuilder();
        for (String part : path.split("[_/]+")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.length() > 0 ? sb.toString() : name;
    }

    protected void m_7856_() {
        ScreenLoop.start();
        List<ClientState.EntryInfo> list = ClientState.entries();
        int pages = Math.max(1, (list.size() + 5 - 1) / 5);
        this.page = Math.max(0, Math.min(this.page, pages - 1));
        this.lastSig = ClientState.signature() + "#" + this.page;
        int x = this.left();
        int y = this.top();
        int start = this.page * 5;
        for (int i = 0; i < 5 && start + i < list.size(); ++i) {
            ClientState.EntryInfo e = list.get(start + i);
            int ry = y + 40 + i * 30;
            this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Release"), btn -> Net.send(4, e.uid())).m_252987_(x + 250 - 108, ry + 4, 50, 20).m_253136_());
            Button digest = Button.m_253074_((Component)Component.m_237113_((String)"Digest"), btn -> Net.send(5, e.uid())).m_252987_(x + 250 - 56, ry + 4, 48, 20).m_253136_();
            digest.f_93623_ = !"minecraft:player".equals(e.typeId());
            this.m_142416_((GuiEventListener)digest);
        }
        int fy = y + 40 + 150 + 6;
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)(ClientState.isHard() ? "Hard mode" : "Soft mode")), btn -> Net.send(1)).m_252987_(x + 8, fy, 76, 20).m_253136_());
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)("Perks (" + ClientState.points() + ")")), btn -> this.f_96541_.m_91152_(PerkMenus.create())).m_252987_(x + 87, fy, 76, 20).m_253136_());
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Storage"), btn -> Net.send(7)).m_252987_(x + 166, fy, 76, 20).m_253136_());
        Button prev = Button.m_253074_((Component)Component.m_237113_((String)"<"), btn -> {
            --this.page;
            this.m_169413_();
            this.m_7856_();
        }).m_252987_(x + 8, fy + 26, 20, 20).m_253136_();
        Button next = Button.m_253074_((Component)Component.m_237113_((String)">"), btn -> {
            ++this.page;
            this.m_169413_();
            this.m_7856_();
        }).m_252987_(x + 32, fy + 26, 20, 20).m_253136_();
        prev.f_93623_ = this.page > 0;
        next.f_93623_ = this.page < pages - 1;
        this.m_142416_((GuiEventListener)prev);
        this.m_142416_((GuiEventListener)next);
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Settings"), btn -> this.f_96541_.m_91152_((Screen)new SettingsScreen())).m_252987_(x + 112, fy + 26, 66, 20).m_253136_());
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237113_((String)"Close"), btn -> this.m_7379_()).m_252987_(x + 250 - 68, fy + 26, 60, 20).m_253136_());
    }

    public void m_86600_() {
        if (!this.lastSig.equals(ClientState.signature() + "#" + this.page)) {
            this.m_169413_();
            this.m_7856_();
        }
    }

    public boolean m_7043_() {
        return false;
    }

    public void m_88315_(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(g);
        int x = this.left();
        int y = this.top();
        g.m_280509_(x, y, x + 250, y + 246, -434891756);
        g.m_280509_(x, y, x + 250, y + 1, -10066330);
        g.m_280509_(x, y + 246 - 1, x + 250, y + 246, -10066330);
        g.m_280509_(x, y, x + 1, y + 246, -10066330);
        g.m_280509_(x + 250 - 1, y, x + 250, y + 246, -10066330);
        g.m_280056_(this.f_96547_, "Stomach  Lv." + ClientState.level(), x + 8, y + 7, 0xFFFFFF, true);
        String legend = "red: health   orange: digestion";
        g.m_280056_(this.f_96547_, legend, x + 250 - 8 - this.f_96547_.m_92895_(legend), y + 7, 0x999999, false);
        int bx = x + 8;
        int bw = 234;
        int by = y + 21;
        g.m_280509_(bx - 1, by - 1, bx + bw + 1, by + 11, -16777216);
        g.m_280509_(bx, by, bx + bw, by + 10, -13421773);
        int fill = (int)((float)bw * Math.min(1.0f, ClientState.used() / ClientState.cap()));
        g.m_280509_(bx, by, bx + fill, by + 10, ClientState.isHard() ? -4173778 : -10635171);
        g.m_280056_(this.f_96547_, Stomach.fmtVolume(ClientState.used()) + " / " + Stomach.fmtVolume(ClientState.cap()), bx + 4, by + 1, 0xFFFFFF, true);
        if (ClientState.predator() > 0) {
            g.m_280056_(this.f_96547_, "Villagers fear you: " + ClientState.predator(), x + 8, y + 32, ClientState.predator() >= 30 ? -38037 : -2056112, false);
        }
        List<ClientState.EntryInfo> list = ClientState.entries();
        int start = this.page * 5;
        if (list.isEmpty()) {
            g.m_280653_(this.f_96547_, (Component)Component.m_237113_((String)"Nothing in your stomach."), x + 125, y + 40 + 60, 0xAAAAAA);
        }
        for (int i = 0; i < 5 && start + i < list.size(); ++i) {
            ClientState.EntryInfo e = list.get(start + i);
            int ry = y + 40 + i * 30;
            g.m_280509_(x + 6, ry + 1, x + 250 - 6, ry + 30 - 1, 0x40FFFFFF);
            ItemStack icon = StomachScreen.iconFor(e.typeId());
            if (!icon.m_41619_()) {
                g.m_280480_(icon, x + 10, ry + 7);
            }
            g.m_280056_(this.f_96547_, this.f_96547_.m_92834_(StomachScreen.displayName(e), 80), x + 32, ry + 4, 0xFFFFFF, true);
            float hpRatio = e.maxHp() > 0.0f ? Math.min(1.0f, e.hp() / e.maxHp()) : 0.0f;
            g.m_280509_(x + 32, ry + 15, x + 112, ry + 19, -14540254);
            g.m_280509_(x + 32, ry + 15, x + 32 + (int)(80.0f * hpRatio), ry + 19, -3129280);
            g.m_280509_(x + 32, ry + 21, x + 112, ry + 24, -14540254);
            g.m_280509_(x + 32, ry + 21, x + 32 + (int)(80.0f * e.digest()), ry + 24, -2060240);
        }
        int pages = Math.max(1, (list.size() + 5 - 1) / 5);
        g.m_280056_(this.f_96547_, "Page " + (this.page + 1) + "/" + pages, x + 58, y + 246 - 20, 0xAAAAAA, true);
        super.m_88315_(g, mouseX, mouseY, partialTick);
    }
}

