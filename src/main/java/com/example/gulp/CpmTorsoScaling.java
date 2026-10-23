package com.example.gulp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Players wearing a Custom Player Model (CPM) that has no belly slider (or whose CPM belly sliders are switched OFF)
 * get their model's own torso scaled forward as they fill up, the same growth the skin belly uses, instead of the
 * skin belly being drawn over the model.
 *
 * It works by scaling the player model's torso part just for the duration of that player's render, then putting it
 * back, so nothing else (arms, legs, other players) is affected.
 */
@Mod.EventBusSubscriber(modid = Gulp.ID, value = Dist.CLIENT)
public final class CpmTorsoScaling {
    private CpmTorsoScaling() { }

    private static ModelPart scaledBody;
    private static float savedZ;

    /** Should this player's CPM torso be scaled right now? */
    private static boolean shouldScale(Player player) {
        boolean isLocal = player == Minecraft.getInstance().player;
        int mode = isLocal ? CpmDriver.localMode() : ClientState.cpmMode(player.getUUID());
        if (mode == CpmDriver.ANIMATED) return false;   // the model's own belly animation does the job
        if (mode == CpmDriver.SCALE) return true;       // model has belly animations, but sliders are OFF
        // No belly animation: scale only if a CPM model has hidden the vanilla torso.
        if (!ClientState.bodyHiddenLastFrame(player.getUUID())) return false;
        return !(isLocal && GulpConfig.bellyStyle == GulpConfig.BellyStyle.OFF);
    }

    @SubscribeEvent
    public static void onPre(RenderPlayerEvent.Pre e) {
        restore(); // safety: never leave a torso scaled
        Player player = e.getEntity();
        if (player.isInvisible()) return;
        float shown = ClientState.peekBelly(player.getUUID());
        if (shown < 0.02f || !shouldScale(player)) return;

        float bulge = (float) Math.sqrt(shown);
        float depthScale = 1f + bulge * BellyLayer.MAX_EXTRA_DEPTH;
        float widthScale = 1f + bulge * BellyLayer.MAX_EXTRA_WIDTH;

        PlayerModel<AbstractClientPlayer> model = e.getRenderer().getModel();
        ModelPart body = model.body;
        scaledBody = body;
        savedZ = body.z;
        body.xScale = widthScale;
        body.zScale = depthScale;
        // The torso is 4 units deep, centred on its pivot. Scaling grows it both ways, so move it forward by the extra
        // so the back stays where it was and the stomach pushes out the front.
        body.z = savedZ - 2f * (depthScale - 1f);
    }

    @SubscribeEvent
    public static void onPost(RenderPlayerEvent.Post e) {
        restore();
    }

    private static void restore() {
        if (scaledBody == null) return;
        scaledBody.xScale = 1f;
        scaledBody.zScale = 1f;
        scaledBody.z = savedZ;
        scaledBody = null;
    }
}
