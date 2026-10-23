/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeDeformation
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 */
package com.example.gulp;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class BellyModel {
    public static final int BELLY_TOP = 5;
    public static final int BELLY_HEIGHT = 6;

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.m_171576_();
        root.m_171599_("belly_full", CubeListBuilder.m_171558_().m_171514_(16, 16).m_171488_(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(0.02f)), PartPose.f_171404_);
        root.m_171599_("jacket_full", CubeListBuilder.m_171558_().m_171514_(16, 32).m_171488_(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(0.27f)), PartPose.f_171404_);
        root.m_171599_("belly_low", CubeListBuilder.m_171558_().m_171514_(16, 21).m_171488_(-4.0f, 5.0f, -2.0f, 8.0f, 6.0f, 4.0f, new CubeDeformation(0.02f)), PartPose.f_171404_);
        root.m_171599_("jacket_low", CubeListBuilder.m_171558_().m_171514_(16, 37).m_171488_(-4.0f, 5.0f, -2.0f, 8.0f, 6.0f, 4.0f, new CubeDeformation(0.27f)), PartPose.f_171404_);
        root.m_171599_("belly_full_clothing", CubeListBuilder.m_171558_().m_171514_(16, 16).m_171488_(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(0.09f)), PartPose.f_171404_);
        root.m_171599_("jacket_full_clothing", CubeListBuilder.m_171558_().m_171514_(16, 32).m_171488_(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(0.33f)), PartPose.f_171404_);
        root.m_171599_("belly_low_clothing", CubeListBuilder.m_171558_().m_171514_(16, 21).m_171488_(-4.0f, 5.0f, -2.0f, 8.0f, 6.0f, 4.0f, new CubeDeformation(0.09f)), PartPose.f_171404_);
        root.m_171599_("jacket_low_clothing", CubeListBuilder.m_171558_().m_171514_(16, 37).m_171488_(-4.0f, 5.0f, -2.0f, 8.0f, 6.0f, 4.0f, new CubeDeformation(0.33f)), PartPose.f_171404_);
        return LayerDefinition.m_171565_((MeshDefinition)mesh, (int)64, (int)64);
    }
}

