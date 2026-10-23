package com.example.gulp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: how this player's Custom Player Model handles the belly (CpmDriver.NONE / ANIMATED / SCALE) and
 *  which belly style this player picked (0 = whole torso, 1 = stomach only). */
public class CpmBellyPacket {
    private final int mode;
    private final int style;

    public CpmBellyPacket(int mode, int style) {
        this.mode = mode;
        this.style = style;
    }

    public static void encode(CpmBellyPacket m, FriendlyByteBuf buf) {
        buf.writeByte(m.mode);
        buf.writeByte(m.style);
    }

    public static CpmBellyPacket decode(FriendlyByteBuf buf) { return new CpmBellyPacket(buf.readByte(), buf.readByte()); }

    public static void handle(CpmBellyPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        ServerPlayer sender = c.getSender();
        c.enqueueWork(() -> {
            if (sender == null) return;
            StomachLogic.setCpmMode(sender, m.mode, m.style);
            StomachLogic.sync(sender, StomachManager.get(sender.getServer()).of(sender.getUUID()));
        });
        c.setPacketHandled(true);
    }
}
