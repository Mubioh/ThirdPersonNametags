package me.mubioh.thirdpersonnametags.mixin;

import me.mubioh.thirdpersonnametags.client.NametagScaleConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    private static final String RENDER_LABEL = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V";

    @Inject(method = RENDER_LABEL, at = @At("HEAD"))
    private void scaleLabelStart(PlayerEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        matrices.push();
        final Vec3d anchor = state.nameLabelPos;
        if (anchor == null) {
            return;
        }
        final float scale = isLocalPlayer(state) ? NametagScaleConfig.selfScale() : NametagScaleConfig.othersScale();
        if (scale == NametagScaleConfig.DEFAULT_SCALE) {
            return;
        }
        matrices.translate(anchor.x, anchor.y, anchor.z);
        matrices.scale(scale, scale, scale);
        matrices.translate(-anchor.x, -anchor.y, -anchor.z);
    }

    @Inject(method = RENDER_LABEL, at = @At("RETURN"))
    private void scaleLabelEnd(PlayerEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        matrices.pop();
    }

    private static boolean isLocalPlayer(PlayerEntityRenderState state) {
        final ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player != null && state.id == player.getId();
    }
}
