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

public class LevelUpPacket {
    private final int level;
    private final int points;

    public LevelUpPacket(int level, int points) {
        this.level = level;
        this.points = points;
    }

    public static void encode(LevelUpPacket m, FriendlyByteBuf buf) {
        buf.writeInt(m.level);
        buf.writeInt(m.points);
    }

    public static LevelUpPacket decode(FriendlyByteBuf buf) {
        return new LevelUpPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(LevelUpPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> ClientState.fireLevelUp(m.level, m.points));
        c.setPacketHandled(true);
    }
}

