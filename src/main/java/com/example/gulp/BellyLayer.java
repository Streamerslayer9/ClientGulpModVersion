/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.PlayerModelPart
 */
package com.example.gulp;

import com.example.gulp.ClientModEvents;
import com.example.gulp.ClientState;
import com.example.gulp.CpmDriver;
import com.example.gulp.GulpConfig;
import com.example.gulp.McaSkins;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;

public class BellyLayer
extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final float MAX_EXTRA_DEPTH = 1.6f;
    private static final float MAX_EXTRA_WIDTH = 0.18f;
    private final ModelPart bellyFull;
    private final ModelPart jacketFull;
    private final ModelPart bellyLow;
    private final ModelPart jacketLow;
    private final ModelPart bellyFullClothing;
    private final ModelPart jacketFullClothing;
    private final ModelPart bellyLowClothing;
    private final ModelPart jacketLowClothing;
    private final RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent;
    private List<Object> mcaLayers;

    public BellyLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        ModelPart root = models.m_171103_(ClientModEvents.BELLY_LAYER);
        this.bellyFull = root.m_171324_("belly_full");
        this.jacketFull = root.m_171324_("jacket_full");
        this.bellyLow = root.m_171324_("belly_low");
        this.jacketLow = root.m_171324_("jacket_low");
        this.bellyFullClothing = root.m_171324_("belly_full_clothing");
        this.jacketFullClothing = root.m_171324_("jacket_full_clothing");
        this.bellyLowClothing = root.m_171324_("belly_low_clothing");
        this.jacketLowClothing = root.m_171324_("jacket_low_clothing");
        this.parent = parent;
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        List<McaSkins.Pass> mca;
        if (player.m_20145_()) {
            return;
        }
        float shown = ClientState.smoothedBelly(player.m_20148_());
        if (shown < 0.02f) {
            return;
        }
        boolean self = player == Minecraft.m_91087_().f_91074_;
        // "Belly style: Off (my model has one)" only hides MY OWN built-in belly.
        // Other players keep theirs unless their own CPM model draws it (reported by their client).
        if (self && GulpConfig.bellyStyle == GulpConfig.BellyStyle.OFF) {
            return;
        }
        if (self && CpmDriver.localModelHandlesBelly() || ClientState.hasCpmModel(player.m_20148_())) {
            return;
        }
        boolean classic = GulpConfig.bellyStyle == GulpConfig.BellyStyle.CLASSIC;
        ModelPart belly = classic ? this.bellyFull : this.bellyLow;
        ModelPart jacket = classic ? this.jacketFull : this.jacketLow;
        float bulge = (float)Math.sqrt(shown);
        float depthScale = 1.0f + bulge * 1.6f;
        float widthScale = 1.0f + bulge * 0.18f;
        poseStack.m_85836_();
        ((PlayerModel)this.m_117386_()).f_102810_.m_104299_(poseStack);
        poseStack.m_252880_(0.0f, 0.0f, 0.125f);
        poseStack.m_85841_(widthScale, 1.0f, depthScale);
        poseStack.m_252880_(0.0f, 0.0f, -0.125f);
        int overlay = LivingEntityRenderer.m_115338_((LivingEntity)player, (float)0.0f);
        boolean showJacket = player.m_36170_(PlayerModelPart.JACKET);
        if (this.mcaLayers == null) {
            this.mcaLayers = McaSkins.findLayers(this.parent);
        }
        if ((mca = McaSkins.passes(player, this.mcaLayers, partialTick)) != null && !mca.isEmpty()) {
            for (McaSkins.Pass pass : mca) {
                ModelPart body;
                VertexConsumer mcaBuffer = buffer.m_6299_(RenderType.m_110458_((ResourceLocation)pass.texture()));
                ModelPart modelPart = pass.clothing() ? (classic ? this.bellyFullClothing : this.bellyLowClothing) : (body = belly);
                ModelPart wear = pass.clothing() ? (classic ? this.jacketFullClothing : this.jacketLowClothing) : jacket;
                body.m_104306_(poseStack, mcaBuffer, packedLight, overlay, pass.r(), pass.g(), pass.b(), 1.0f);
                if (!showJacket) continue;
                wear.m_104306_(poseStack, mcaBuffer, packedLight, overlay, pass.r(), pass.g(), pass.b(), 1.0f);
            }
        } else {
            VertexConsumer vc = buffer.m_6299_(RenderType.m_110458_((ResourceLocation)player.m_108560_()));
            belly.m_104301_(poseStack, vc, packedLight, overlay);
            if (showJacket) {
                jacket.m_104301_(poseStack, vc, packedLight, overlay);
            }
        }
        poseStack.m_85849_();
    }
}

