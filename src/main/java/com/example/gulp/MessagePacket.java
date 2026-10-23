/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.example.gulp;

import com.example.gulp.ClientState;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

public class MessagePacket {
    private final Component text;

    public MessagePacket(Component text) {
        this.text = text;
    }

    public static void encode(MessagePacket m, FriendlyByteBuf buf) {
        buf.m_130083_(m.text);
    }

    public static MessagePacket decode(FriendlyByteBuf buf) {
        return new MessagePacket(buf.m_130238_());
    }

    public static void handle(MessagePacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> ClientState.fireMessage(m.text));
        c.setPacketHandled(true);
    }
}

