package com.example.gulp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: how this player's Custom Player Model handles the belly (CpmDriver.NONE / ANIMATED / SCALE). */
public class CpmBellyPacket {
    private final int mode;

    public CpmBellyPacket(int mode) { this.mode = mode; }

    public static void encode(CpmBellyPacket m, FriendlyByteBuf buf) { buf.writeByte(m.mode); }

    public static CpmBellyPacket decode(FriendlyByteBuf buf) { return new CpmBellyPacket(buf.readByte()); }

    public static void handle(CpmBellyPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        ServerPlayer sender = c.getSender();
        c.enqueueWork(() -> {
            if (sender == null) return;
            StomachLogic.setCpmMode(sender, m.mode);
            StomachLogic.sync(sender, StomachManager.get(sender.getServer()).of(sender.getUUID()));
        });
        c.setPacketHandled(true);
    }
}
