package com.example.gulp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: this player's Custom Player Model does (or no longer does) draw its own belly. */
public class CpmBellyPacket {
    private final boolean hasBelly;

    public CpmBellyPacket(boolean hasBelly) { this.hasBelly = hasBelly; }

    public static void encode(CpmBellyPacket m, FriendlyByteBuf buf) { buf.writeBoolean(m.hasBelly); }

    public static CpmBellyPacket decode(FriendlyByteBuf buf) { return new CpmBellyPacket(buf.readBoolean()); }

    public static void handle(CpmBellyPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        ServerPlayer sender = c.getSender();
        c.enqueueWork(() -> {
            if (sender == null) return;
            StomachLogic.setCpmBelly(sender, m.hasBelly);
            StomachLogic.sync(sender, StomachManager.get(sender.getServer()).of(sender.getUUID()));
        });
        c.setPacketHandled(true);
    }
}
