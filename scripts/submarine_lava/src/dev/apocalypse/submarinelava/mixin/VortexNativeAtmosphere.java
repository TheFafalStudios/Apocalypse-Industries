package dev.apocalypse.submarinelava.mixin;
import dev.apocalypse.submarinelava.VortexBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets={"com.maxenonyme.vortexengine.atmosphere.Atmosphere","com.maxenonyme.vortexengine.mist.MistRenderer"},remap=false)
public class VortexNativeAtmosphere {
 @Inject(method="onRenderStage",at=@At("HEAD"),cancellable=true)
 private static void ai$shaderOwnsAtmosphere(CallbackInfo ci) {
  if(VortexBridge.active())ci.cancel();
 }
}
