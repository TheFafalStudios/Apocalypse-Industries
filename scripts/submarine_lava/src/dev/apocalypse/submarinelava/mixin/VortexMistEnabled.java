package dev.apocalypse.submarinelava.mixin;
import dev.apocalypse.submarinelava.VortexBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets="com.maxenonyme.vortexengine.mist.MistField",remap=false)
public class VortexMistEnabled {
 @Inject(method="usable",at=@At("RETURN"),cancellable=true)
 private static void ai$mist(CallbackInfoReturnable<Boolean> ci) {
  if(VortexBridge.mist())ci.setReturnValue(true);
 }
}
