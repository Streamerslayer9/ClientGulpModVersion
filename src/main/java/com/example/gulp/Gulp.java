/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.fml.ModLoadingContext
 *  net.minecraftforge.fml.common.Mod
 *  net.minecraftforge.fml.config.IConfigSpec
 *  net.minecraftforge.fml.config.ModConfig$Type
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.example.gulp;

import com.example.gulp.GulpServerConfig;
import com.example.gulp.Net;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

@Mod(value="gulp")
public class Gulp {
    public static final String ID = "gulp";
    public static final String STORAGE_TITLE = "Stomach Storage";
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.SOUND_EVENTS, (String)"gulp");
    public static final RegistryObject<SoundEvent> SWALLOW = Gulp.sound("swallow");
    public static final RegistryObject<SoundEvent> DIGEST = Gulp.sound("digest");
    public static final RegistryObject<SoundEvent> RELEASE = Gulp.sound("release");
    public static final RegistryObject<SoundEvent> DIGEST_BUTTON = Gulp.sound("digest_button");
    public static final RegistryObject<SoundEvent> SCREEN_LOOP = Gulp.sound("screen_loop");
    public static final RegistryObject<SoundEvent> SWALLOW_LOUD = Gulp.sound("swallow_loud");
    public static final RegistryObject<SoundEvent> RELEASE_LOUD = Gulp.sound("release_loud");
    public static final RegistryObject<SoundEvent> DIGEST_LOUD = Gulp.sound("digest_loud");
    public static final RegistryObject<SoundEvent> DIGEST_BUTTON_LOUD = Gulp.sound("digest_button_loud");

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation(ID, name)));
    }

    public Gulp() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        SOUNDS.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, (IConfigSpec)GulpServerConfig.SPEC);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(Net::register);
    }
}

