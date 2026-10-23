package com.example.gulp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Tells clients how full a player's belly is (0..1) so everyone sees the same bulge, and how that player's
 *  Custom Player Model handles the belly (0 none, 1 its own animation, 2 scale the model's torso). */
public class BellyPacket {
    private final UUID player;
    private final float ratio;
    private final int cpmMode;

    public BellyPacket(UUID player, float ratio, int cpmMode) {
        this.player = player;
        this.ratio = ratio;
        this.cpmMode = cpmMode;
    }

    public static void encode(BellyPacket m, FriendlyByteBuf buf) {
        buf.writeUUID(m.player);
        buf.writeFloat(m.ratio);
        buf.writeByte(m.cpmMode);
    }

    public static BellyPacket decode(FriendlyByteBuf buf) {
        return new BellyPacket(buf.readUUID(), buf.readFloat(), buf.readByte());
    }

    public static void handle(BellyPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ClientState.setBelly(m.player, m.ratio);
            ClientState.setCpmMode(m.player, m.cpmMode);
        });
        c.setPacketHandled(true);
    }
}
