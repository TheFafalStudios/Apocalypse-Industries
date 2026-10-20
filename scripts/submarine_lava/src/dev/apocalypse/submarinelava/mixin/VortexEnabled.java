package dev.apocalypse.submarinelava.mixin;
import dev.apocalypse.submarinelava.VortexBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets="com.maxenonyme.vortexengine.light.VortexLights",remap=false)
public class VortexEnabled {
 @Inject(method="usable",at=@At("RETURN"),cancellable=true)
 private static void ai$lights(CallbackInfoReturnable<Boolean> ci) {
  if(VortexBridge.lights())ci.setReturnValue(true);
 }
}
