/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.ContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.Slot
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ScreenEvent$Init$Post
 *  net.minecraftforge.client.event.ScreenEvent$KeyPressed$Pre
 *  net.minecraftforge.client.event.ScreenEvent$Render$Post
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.example.gulp;

import com.example.gulp.ClientModEvents;
import com.example.gulp.ClientState;
import com.example.gulp.Net;
import com.example.gulp.Perk;
import com.example.gulp.StomachItems;
import com.example.gulp.StomachScreen;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="gulp", value={Dist.CLIENT})
public final class ClientScreenEvents {
    private static final int BAR_W = 90;

    private ClientScreenEvents() {
    }

    @SubscribeEvent
    public static void onInventoryKey(ScreenEvent.KeyPressed.Pre e) {
        Screen screen = e.getScreen();
        if (!(screen instanceof InventoryScreen)) {
            return;
        }
        InventoryScreen screen2 = (InventoryScreen)screen;
        if (!ClientModEvents.INVENTORY_SWALLOW_KEY.isActiveAndMatches(InputConstants.m_84827_((int)e.getKeyCode(), (int)e.getScanCode()))) {
            return;
        }
        Slot slot = screen2.getSlotUnderMouse();
        if (slot == null || !slot.m_6657_() || !(slot.f_40218_ instanceof Inventory) || slot.getSlotIndex() >= 36) {
            return;
        }
        Net.send(9, slot.f_40219_ | (Screen.m_96637_() ? 65536 : 0));
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post e) {
        Screen screen = e.getScreen();
        if (!(screen instanceof ContainerScreen)) {
            return;
        }
        ContainerScreen screen2 = (ContainerScreen)screen;
        if (!"Stomach Storage".equals(screen2.m_96636_().getString())) {
            return;
        }
        int width = 52;
        int x = screen2.getGuiLeft() - width - 4;
        if (x < 2) {
            x = screen2.getGuiLeft() + screen2.getXSize() + 4;
        }
        Button back = Button.m_253074_((Component)Component.m_237113_((String)"Back"), btn -> {
            Minecraft mc = Minecraft.m_91087_();
            if (mc.f_91074_ == null) {
                return;
            }
            mc.f_91074_.m_6915_();
            mc.m_91152_((Screen)new StomachScreen());
        }).m_252987_(x, screen2.getGuiTop(), width, 20).m_253136_();
        e.addListener((GuiEventListener)back);
        int bw = 34;
        int bh = 12;
        int by = screen2.getGuiTop() + 3;
        int right = screen2.getGuiLeft() + screen2.getXSize() - 7;
        SmallTextButton digest64 = new SmallTextButton(right - bw, by, bw, bh, (Component)Component.m_237113_((String)"Eat 64"), btn -> Net.send(10, 64));
        digest64.m_257544_(Tooltip.m_257550_((Component)Component.m_237113_((String)"Eat up to 64 of the item in the first slot")));
        SmallTextButton digest1 = new SmallTextButton(right - bw * 2 - 2, by, bw, bh, (Component)Component.m_237113_((String)"Eat 1"), btn -> Net.send(10, 1));
        digest1.m_257544_(Tooltip.m_257550_((Component)Component.m_237113_((String)"Eat 1 of the item in the first slot")));
        e.addListener((GuiEventListener)digest1);
        e.addListener((GuiEventListener)digest64);
    }

    private static int trackerY(ContainerScreen screen) {
        int y = screen.getGuiTop() + screen.getYSize() + 4;
        return y + 22 > screen.f_96544_ ? Math.max(2, screen.getGuiTop() - 24) : y;
    }

    @SubscribeEvent
    public static void onStorageRender(ScreenEvent.Render.Post e) {
        Screen screen = e.getScreen();
        if (!(screen instanceof ContainerScreen)) {
            return;
        }
        ContainerScreen screen2 = (ContainerScreen)screen;
        if (!"Stomach Storage".equals(screen2.m_96636_().getString())) {
            return;
        }
        Font font = Minecraft.m_91087_().f_91062_;
        GuiGraphics g = e.getGuiGraphics();
        int progress = ClientState.digestProgress();
        int threshold = 32;
        String text = "Digestion Progress: " + progress + " / " + threshold + " Items";
        int w = Math.max(font.m_92895_(text), 90);
        int x = screen2.getGuiLeft() + (screen2.getXSize() - w) / 2;
        int y = ClientScreenEvents.trackerY(screen2);
        g.m_280056_(font, text, x, y, 0xFFFFFF, true);
        int by = y + 11;
        g.m_280509_(x, by, x + w, by + 4, -16777216);
        g.m_280509_(x + 1, by + 1, x + w - 1, by + 3, -13421773);
        int fill = (int)((float)(w - 2) * Math.min(1.0f, (float)progress / (float)threshold));
        g.m_280509_(x + 1, by + 1, x + 1 + fill, by + 3, -10635171);
        if (e.getMouseX() >= x && e.getMouseX() <= x + w && e.getMouseY() >= y && e.getMouseY() <= by + 4) {
            float bars = StomachItems.hungerBars(ClientState.rank(Perk.UNIVERSAL));
            String tip = String.format("Eats %d items to restore hunger. Current Payout: %.1f Hunger Bars per %d items.", threshold, Float.valueOf(bars), threshold);
            g.m_280245_(font, font.m_92923_((FormattedText)Component.m_237113_((String)tip), 190), e.getMouseX(), e.getMouseY());
        }
    }

    private static final class SmallTextButton
    extends Button {
        private static final float TEXT_SCALE = 0.8f;

        SmallTextButton(int x, int y, int w, int h, Component label, Button.OnPress onPress) {
            super(x, y, w, h, label, onPress, Button.f_252438_);
        }

        public void m_280139_(GuiGraphics g, Font font, int color) {
            Component label = this.m_6035_();
            float cx = (float)this.m_252754_() + (float)this.m_5711_() / 2.0f;
            float f = this.m_252907_();
            float f2 = this.m_93694_();
            Objects.requireNonNull(font);
            float cy = f + (f2 - 9.0f * 0.8f) / 2.0f + 0.5f;
            g.m_280168_().m_85836_();
            g.m_280168_().m_252880_(cx, cy, 0.0f);
            g.m_280168_().m_85841_(0.8f, 0.8f, 1.0f);
            g.m_280430_(font, label, -font.m_92852_((FormattedText)label) / 2, 0, color);
            g.m_280168_().m_85849_();
        }
    }
}

