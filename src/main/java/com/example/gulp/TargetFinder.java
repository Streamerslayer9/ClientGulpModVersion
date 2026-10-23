/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.boss.enderdragon.EnderDragon
 *  net.minecraft.world.entity.boss.wither.WitherBoss
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.example.gulp;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class TargetFinder {
    public static final double REACH = 4.0;
    private static final double GRACE = 0.3;
    private static final double ASSIST_COS = Math.cos(Math.toRadians(12.0));

    private TargetFinder() {
    }

    public static boolean valid(Entity e) {
        if (e instanceof ItemEntity) {
            return e.m_6084_();
        }
        return e instanceof LivingEntity && e.m_6084_() && !e.m_5833_() && !(e instanceof EnderDragon) && !(e instanceof WitherBoss);
    }

    public static Entity find(Player p, double reach) {
        Vec3 eye = p.m_146892_();
        Vec3 look = p.m_20252_(1.0f);
        Vec3 end = eye.m_82549_(look.m_82490_(reach));
        AABB area = p.m_20191_().m_82369_(look.m_82490_(reach)).m_82400_(1.5);
        Entity direct = null;
        double directDist = Double.MAX_VALUE;
        Entity assist = null;
        double bestDot = ASSIST_COS;
        for (Entity e : p.m_9236_().m_6249_((Entity)p, area, TargetFinder::valid)) {
            double dot;
            AABB box = e.m_20191_().m_82400_(0.3);
            if (box.m_82390_(eye)) {
                return e;
            }
            Optional hit = box.m_82371_(eye, end);
            if (hit.isPresent()) {
                double d = eye.m_82557_((Vec3)hit.get());
                if (!(d < directDist)) continue;
                directDist = d;
                direct = e;
                continue;
            }
            Vec3 toCenter = e.m_20191_().m_82399_().m_82546_(eye);
            double len = toCenter.m_82553_();
            if (!(len > 0.001) || !(len <= reach + 1.0) || !((dot = toCenter.m_82490_(1.0 / len).m_82526_(look)) > bestDot)) continue;
            bestDot = dot;
            assist = e;
        }
        return direct != null ? direct : assist;
    }

    public static boolean withinReach(Player p, Entity e, double slack) {
        Vec3 eye = p.m_146892_();
        AABB b = e.m_20191_();
        double x = Mth.m_14008_((double)eye.f_82479_, (double)b.f_82288_, (double)b.f_82291_);
        double y = Mth.m_14008_((double)eye.f_82480_, (double)b.f_82289_, (double)b.f_82292_);
        double z = Mth.m_14008_((double)eye.f_82481_, (double)b.f_82290_, (double)b.f_82293_);
        double max = 4.0 + slack;
        return eye.m_82531_(x, y, z) <= max * max;
    }
}

