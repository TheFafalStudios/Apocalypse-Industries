package dev.apocalypse.submarinelava.mixin;
import dev.apocalypse.submarinelava.LavaSamplers;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets = "net.irisshaders.iris.gl.program.ProgramSamplers$Builder", remap = false)
public abstract class LavaSampler {
    @Inject(method = "build", at = @At("HEAD"))
    private void ai$sampler(CallbackInfoReturnable<ProgramSamplers> ci) {
        // Standard samplers, including mandatory unit 0, have already been assigned.
        LavaSamplers.register((ProgramSamplers.Builder)(Object)this);
    }
}
