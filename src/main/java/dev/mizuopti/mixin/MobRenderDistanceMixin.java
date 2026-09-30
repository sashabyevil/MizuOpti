package dev.mizuopti.mixin;

import dev.mizuopti.ClientEntityRenderConfig;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
abstract class MobRenderDistanceMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void mizuopti$cullDistantMobs(
            E entity,
            Frustum frustum,
            double cameraX,
            double cameraY,
            double cameraZ,
            CallbackInfoReturnable<Boolean> callbackInfo) {
        if (entity instanceof Mob mob && ClientEntityRenderConfig.shouldCull(mob, cameraX, cameraY, cameraZ)) {
            callbackInfo.setReturnValue(false);
        }
    }
}