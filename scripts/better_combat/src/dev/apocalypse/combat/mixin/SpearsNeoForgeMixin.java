package dev.apocalypse.combat.mixin;
import com.notunanancyowen.spears.Spears;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets="com.notunanancyowen.spears.neoforge.SpearsNeoForge", remap=false)
public abstract class SpearsNeoForgeMixin {
 @Inject(method="<init>", at=@At("RETURN"))
 private void retainNativeSpearInput(CallbackInfo ci) {
  // Native stab packets already apply on-swing effects; BC's bridge duplicates Lunge.
  Spears.hasBetterCombat=false;
 }
}
