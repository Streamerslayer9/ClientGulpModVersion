package com.example.gulp;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

/**
 * Tells everyone whether a player's CPM model draws its own belly.
 * Client -> server: the player reports their own state (the UUID field is ignored).
 * Server -> clients: the server rebroadcasts it with the real UUID.
 */
public class CpmFlagPacket {
    private final UUID player;
    private final boolean handlesBelly;

    public CpmFlagPacket(UUID player, boolean handlesBelly) {
        this.player = player;
        this.handlesBelly = handlesBelly;
    }

    public static void encode(CpmFlagPacket m, FriendlyByteBuf buf) {
        buf.m_130077_(m.player);
        buf.writeBoolean(m.handlesBelly);
    }

    public static CpmFlagPacket decode(FriendlyByteBuf buf) {
        return new CpmFlagPacket(buf.m_130259_(), buf.readBoolean());
    }

    public static void handle(CpmFlagPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        ServerPlayer sender = c.getSender();
        if (sender != null) {
            boolean flag = m.handlesBelly;
            c.enqueueWork(() -> Net.CHANNEL.send(PacketDistributor.ALL.noArg(), new CpmFlagPacket(sender.m_20148_(), flag)));
        } else {
            c.enqueueWork(() -> ClientState.setCpmModel(m.player, m.handlesBelly));
        }
        c.setPacketHandled(true);
    }
}
