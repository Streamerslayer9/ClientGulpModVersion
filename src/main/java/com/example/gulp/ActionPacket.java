/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.example.gulp;

import com.example.gulp.StomachLogic;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ActionPacket {
    private final int action;
    private final int arg;

    public ActionPacket(int action, int arg) {
        this.action = action;
        this.arg = arg;
    }

    public static void encode(ActionPacket m, FriendlyByteBuf buf) {
        buf.writeByte(m.action);
        buf.writeInt(m.arg);
    }

    public static ActionPacket decode(FriendlyByteBuf buf) {
        return new ActionPacket(buf.readByte(), buf.readInt());
    }

    public static void handle(ActionPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        ServerPlayer sender = c.getSender();
        c.enqueueWork(() -> StomachLogic.handleAction(sender, m.action, m.arg));
        c.setPacketHandled(true);
    }
}

