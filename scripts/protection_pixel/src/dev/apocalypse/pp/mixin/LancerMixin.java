package dev.apocalypse.pp.mixin;
import dev.apocalypse.pp.LancerSafety;
import net.mcreator.protectionpixel.procedures.LancerfProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=LancerfProcedure.class, remap=false)
public class LancerMixin {
 @Inject(method="execute", at=@At("HEAD"), cancellable=true, remap=false)
 private static void sample(double x,double y,double z,Entity e,ItemStack s,CallbackInfo ci) { LancerSafety.sample(e,s); ci.cancel(); }
}
