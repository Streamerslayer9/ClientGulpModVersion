package com.example.gulp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Tells clients how full a player's belly is (0..1) so everyone sees the same bulge, and how that player's
 *  Custom Player Model handles the belly (0 none, 1 its own animation, 2 scale the model's torso),
 *  and which belly style that player picked (each player's choice applies only to their own belly). */
public class BellyPacket {
    private final UUID player;
    private final float ratio;
    private final int cpmMode;
    private final int style;

    public BellyPacket(UUID player, float ratio, int cpmMode, int style) {
        this.player = player;
        this.ratio = ratio;
        this.cpmMode = cpmMode;
        this.style = style;
    }

    public static void encode(BellyPacket m, FriendlyByteBuf buf) {
        buf.writeUUID(m.player);
        buf.writeFloat(m.ratio);
        buf.writeByte(m.cpmMode);
        buf.writeByte(m.style);
    }

    public static BellyPacket decode(FriendlyByteBuf buf) {
        return new BellyPacket(buf.readUUID(), buf.readFloat(), buf.readByte(), buf.readByte());
    }

    public static void handle(BellyPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ClientState.setBelly(m.player, m.ratio);
            ClientState.setCpmMode(m.player, m.cpmMode);
            ClientState.setStyle(m.player, m.style);
        });
        c.setPacketHandled(true);
    }
}
