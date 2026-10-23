/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package com.example.gulp;

import com.example.gulp.ActionPacket;
import com.example.gulp.BellyPacket;
import com.example.gulp.LevelUpPacket;
import com.example.gulp.MessagePacket;
import com.example.gulp.SoundPacket;
import com.example.gulp.SyncPacket;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Net {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("gulp", "main"), () -> "1", "1"::equals, "1"::equals);

    private Net() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, ActionPacket.class, ActionPacket::encode, ActionPacket::decode, ActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, SyncPacket.class, SyncPacket::encode, SyncPacket::decode, SyncPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, BellyPacket.class, BellyPacket::encode, BellyPacket::decode, BellyPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, SoundPacket.class, SoundPacket::encode, SoundPacket::decode, SoundPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, MessagePacket.class, MessagePacket::encode, MessagePacket::decode, MessagePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, LevelUpPacket.class, LevelUpPacket::encode, LevelUpPacket::decode, LevelUpPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, CpmFlagPacket.class, CpmFlagPacket::encode, CpmFlagPacket::decode, CpmFlagPacket::handle, Optional.empty());
    }

    public static void send(int action) {
        Net.send(action, 0);
    }

    public static void send(int action, int arg) {
        CHANNEL.sendToServer((Object)new ActionPacket(action, arg));
    }
}

