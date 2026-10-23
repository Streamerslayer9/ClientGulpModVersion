package com.example.gulp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Tells clients how full a player's belly is (0..1) so everyone sees the same bulge, and whether that player's
 *  Custom Player Model draws its own belly (then nobody draws the built-in skin bulge on top of it). */
public class BellyPacket {
    private final UUID player;
    private final float ratio;
    private final boolean cpmBelly;

    public BellyPacket(UUID player, float ratio, boolean cpmBelly) {
        this.player = player;
        this.ratio = ratio;
        this.cpmBelly = cpmBelly;
    }

    public static void encode(BellyPacket m, FriendlyByteBuf buf) {
        buf.writeUUID(m.player);
        buf.writeFloat(m.ratio);
        buf.writeBoolean(m.cpmBelly);
    }

    public static BellyPacket decode(FriendlyByteBuf buf) {
        return new BellyPacket(buf.readUUID(), buf.readFloat(), buf.readBoolean());
    }

    public static void handle(BellyPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ClientState.setBelly(m.player, m.ratio);
            ClientState.setCpmBelly(m.player, m.cpmBelly);
        });
        c.setPacketHandled(true);
    }
}
