package me.mubioh.thirdpersonnametags.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true)
    private void viewOwnLabel(LivingEntity entity, double squaredDistanceToCamera, CallbackInfoReturnable<Boolean> cir) {
        if (cir.isCancelled()) return;

        final MinecraftClient client = MinecraftClient.getInstance();
        final LivingEntity player = client.player;

        if (player != null && entity == player) {
            // Check that the HUD is enabled, the player is in third person, and no GUI/screen is currently open
            boolean shouldShow = MinecraftClient.isHudEnabled()
                    && !client.options.getPerspective().isFirstPerson()
                    && client.currentScreen == null;

            cir.setReturnValue(shouldShow);
        }
    }
}
