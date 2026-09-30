package dev.mizuopti.mixin;

import dev.mizuopti.EntityOptimizationConfig;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
abstract class MobAiTickMixin {
    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void mizuopti$throttleDistantAi(CallbackInfo callbackInfo) {
        if (EntityOptimizationConfig.shouldSkipAi((Mob) (Object) this)) {
            callbackInfo.cancel();
        }
    }
}