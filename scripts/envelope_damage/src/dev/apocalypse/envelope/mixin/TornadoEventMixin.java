package dev.apocalypse.envelope.mixin;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.apocalypse.envelope.DamagePolicy;
import dev.apocalypse.envelope.Settings;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
/** Replace envelope eligibility only; retain upstream rays and non-envelope destruction. */
@Mixin(targets="net.killey.tornadophysics.event.TornadoEvent",remap=false)
public abstract class TornadoEventMixin {
 @ModifyExpressionValue(method="processBlockDestruction",at=@At(value="INVOKE",target="Ljava/util/stream/Stream;anyMatch(Ljava/util/function/Predicate;)Z"),require=1,expect=1,allow=1)
 private boolean apocalypse$envelopeTag(boolean original,ServerSubLevel ship,ServerLevel world,
   @Local BlockState state,@Local BlockPos position) {
  if(!Settings.ENABLED.get()||!DamagePolicy.isEnvelope(state))return original;
  boolean listed=net.killey.tornadophysics.Config.DESTROYABLE_BLOCKS.get().contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
  if(original||listed)DamagePolicy.tryPuncture(ship,world,position);
  return false;
 }
 @ModifyExpressionValue(method="processBlockDestruction",at=@At(value="INVOKE",target="Ljava/util/List;contains(Ljava/lang/Object;)Z"),require=1,expect=1,allow=1)
 private boolean apocalypse$envelopeId(boolean original,@Local BlockState state) {
  return Settings.ENABLED.get()&&DamagePolicy.isEnvelope(state)?false:original;
 }
}
