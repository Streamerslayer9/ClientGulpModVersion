/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.example.gulp;

import com.example.gulp.ClientState;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SoundPacket {
    private final int type;
    private final double x;
    private final double y;
    private final double z;

    public SoundPacket(int type, double x, double y, double z) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(SoundPacket m, FriendlyByteBuf buf) {
        buf.writeInt(m.type);
        buf.writeDouble(m.x);
        buf.writeDouble(m.y);
        buf.writeDouble(m.z);
    }

    public static SoundPacket decode(FriendlyByteBuf buf) {
        return new SoundPacket(buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(SoundPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> ClientState.firePlaySound(m.type, m.x, m.y, m.z));
        c.setPacketHandled(true);
    }
}

