package dev.apocalypse.submarinelava.mixin;
import dev.apocalypse.submarinelava.LavaVisuals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets = "com.maxenonyme.createsubmarine.submarine.client.SubmarineFogHandler", remap = false)
public abstract class FogState {
    @Inject(method = "onClientTick", at = @At("TAIL"))
    private static void ai$update(CallbackInfo ci) { LavaVisuals.update(); }
}
