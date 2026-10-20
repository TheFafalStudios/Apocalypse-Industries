package dev.apocalypse.submarinelava.mixin;
import dev.apocalypse.submarinelava.ShaderPatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets = "com.maxenonyme.createsubmarine.submarine.compat.iris.ShaderpackSupport", remap = false)
public abstract class HullBinding {
    @Inject(method = "patchFragment", at = @At("RETURN"), cancellable = true)
    private static void ai$patch(String source, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(ShaderPatch.patch(cir.getReturnValue()));
    }
}
