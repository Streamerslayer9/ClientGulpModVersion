/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Component$Serializer
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.example.gulp;

import com.example.gulp.ClientState;
import com.example.gulp.GulpServerConfig;
import com.example.gulp.Perk;
import com.example.gulp.Stomach;
import com.example.gulp.StomachLogic;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.network.NetworkEvent;

public class SyncPacket {
    public boolean hard;
    public int level;
    public int xp;
    public int xpNeeded;
    public int points;
    public boolean allowSwallowed;
    public int predator;
    public float itemFill;
    public int digestProgress;
    public float used;
    public float cap;
    public int[] ranks = new int[Perk.values().length];
    public List<ClientState.EntryInfo> entries = new ArrayList<ClientState.EntryInfo>();

    public static SyncPacket from(Stomach s) {
        SyncPacket m = new SyncPacket();
        m.hard = s.hard && (Boolean)GulpServerConfig.HARD_MODE_ENABLED.get() != false;
        m.level = s.level;
        m.xp = s.xp;
        m.xpNeeded = Stomach.xpForNext(s.level);
        m.used = (float)s.used();
        m.cap = (float)s.capacity();
        m.points = s.points;
        m.allowSwallowed = s.allowSwallowed;
        m.predator = s.predator;
        m.itemFill = s.itemFill();
        m.digestProgress = s.digestProgress;
        for (Perk p : Perk.values()) {
            m.ranks[p.ordinal()] = s.rank(p);
        }
        for (CompoundTag e : s.contents) {
            CompoundTag ent = e.m_128469_("Entity");
            int dt = Math.max(1, e.m_128451_("DigestTime"));
            m.entries.add(new ClientState.EntryInfo(e.m_128451_("Uid"), StomachLogic.entryName(e), ent.m_128461_("id"), e.m_128457_("Size"), ent.m_128457_("Health"), e.m_128457_("MaxHealth"), Math.min(1.0f, (float)e.m_128451_("Digest") / (float)dt)));
        }
        return m;
    }

    public static void encode(SyncPacket m, FriendlyByteBuf buf) {
        buf.writeBoolean(m.hard);
        buf.writeInt(m.level);
        buf.writeInt(m.xp);
        buf.writeInt(m.xpNeeded);
        buf.writeFloat(m.used);
        buf.writeFloat(m.cap);
        buf.writeInt(m.points);
        buf.writeBoolean(m.allowSwallowed);
        buf.writeInt(m.predator);
        buf.writeFloat(m.itemFill);
        buf.writeInt(m.digestProgress);
        buf.writeInt(m.ranks.length);
        for (int r : m.ranks) {
            buf.writeInt(r);
        }
        buf.writeInt(m.entries.size());
        java.util.Iterator<ClientState.EntryInfo> object = m.entries.iterator();
        while (object.hasNext()) {
            ClientState.EntryInfo e = object.next();
            buf.writeInt(e.uid());
            buf.m_130070_(Component.Serializer.m_130703_((Component)e.name()));
            buf.m_130070_(e.typeId());
            buf.writeFloat(e.size());
            buf.writeFloat(e.hp());
            buf.writeFloat(e.maxHp());
            buf.writeFloat(e.digest());
        }
    }

    public static SyncPacket decode(FriendlyByteBuf buf) {
        SyncPacket m = new SyncPacket();
        m.hard = buf.readBoolean();
        m.level = buf.readInt();
        m.xp = buf.readInt();
        m.xpNeeded = buf.readInt();
        m.used = buf.readFloat();
        m.cap = buf.readFloat();
        m.points = buf.readInt();
        m.allowSwallowed = buf.readBoolean();
        m.predator = buf.readInt();
        m.itemFill = buf.readFloat();
        m.digestProgress = buf.readInt();
        int perkCount = buf.readInt();
        for (int i = 0; i < perkCount; ++i) {
            int v = buf.readInt();
            if (i >= m.ranks.length) continue;
            m.ranks[i] = v;
        }
        int n = buf.readInt();
        for (int i = 0; i < n; ++i) {
            int uid = buf.readInt();
            MutableComponent name = Component.Serializer.m_130701_((String)buf.m_130277_());
            if (name == null) {
                name = Component.m_237119_();
            }
            String type = buf.m_130277_();
            float size = buf.readFloat();
            float hp = buf.readFloat();
            float maxHp = buf.readFloat();
            float dig = buf.readFloat();
            m.entries.add(new ClientState.EntryInfo(uid, (Component)name, type, size, hp, maxHp, dig));
        }
        return m;
    }

    public static void handle(SyncPacket m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> ClientState.apply(m));
        c.setPacketHandled(true);
    }
}

