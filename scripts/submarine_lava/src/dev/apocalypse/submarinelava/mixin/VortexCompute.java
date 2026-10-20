package dev.apocalypse.submarinelava.mixin;
import dev.apocalypse.submarinelava.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets="com.maxenonyme.vortexengine.light.LightVolumes",remap=false)
public class VortexCompute {
 private static long ai$lastUpdate;
 private static Object ai$lastLevel;
 @WrapMethod(method="onRenderStage")
 private static void ai$preserveState(RenderLevelStageEvent event,Operation<Void> original) {
  if(VortexBridge.active() && net.irisshaders.iris.api.v0.IrisApi.getInstance().isRenderingShadowPass())return;
  if(!VortexBridge.active() || event.getStage()!=RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES){original.call(event);return;}
  // Bound native 128^3 propagation and GL-state snapshots to 12.5 updates/second.
  // Rendering still samples the previous field every frame.
  long now=System.nanoTime();
  Object level=net.minecraft.client.Minecraft.getInstance().level;
  if(level==ai$lastLevel && now-ai$lastUpdate<80_000_000L)return;
  ai$lastLevel=level;ai$lastUpdate=now;
  VortexBridge.updates++;
  try(VortexRenderState state=new VortexRenderState()){original.call(event);}
 }
 @Redirect(method="onRenderStage",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/common/ModConfigSpec$EnumValue;get()Ljava/lang/Object;",ordinal=0))
 private static Object ai$fieldStyle(net.neoforged.neoforge.common.ModConfigSpec.EnumValue<?> option){
  Object style=option.get();
  if(VortexBridge.active() && (style==com.maxenonyme.vortexengine.VortexConfig.LightStyle.VOLUMES || style==com.maxenonyme.vortexengine.VortexConfig.LightStyle.PIXEL))
   return com.maxenonyme.vortexengine.VortexConfig.LightStyle.FLOOD;
  return style;
 }
 @Inject(method="draw",at=@At("HEAD"),cancellable=true)
 private static void ai$noSecondRenderer(RenderLevelStageEvent event,Vec3 camera,CallbackInfo ci) {
  if(VortexBridge.active())ci.cancel();
 }
}
