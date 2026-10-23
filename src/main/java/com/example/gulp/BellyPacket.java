/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.example.gulp;

import com.example.gulp.ClientState;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class BellyPacket {
    private final UUID player;
    private final float ratio;

    public BellyPacket(UUID player, float ratio) {
        this.player = player;
        this.ratio = ratio;
    }

    public static void encode(BellyPacket m, FriendlyByteBuf buf) {
        buf.m_130077_(m.player);
        buf.writeFloat(m.ratio);
    }

    public static BellyPacket decode(FriendlyByteBuf buf) {
        return new BellyPacket(buf.m_130259_(), buf.readFloat());
    }

    public static void handle(BellyPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> ClientState.setBelly(m.player, m.ratio));
        c.setPacketHandled(true);
    }
}

