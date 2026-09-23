package dev.apocalypse.pp.mixin;
import dev.apocalypse.pp.ConsumableRepairRules;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
@Pseudo
@Mixin(targets="com.cozary.nameless_trinkets.items.trinkets.Reforger",remap=false)
public abstract class ReforgerMixin {
 @Redirect(method="curioTick",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;isDamaged()Z"),require=1)
 private boolean skipConsumables(ItemStack stack) {
  return !ConsumableRepairRules.restricted(stack) && stack.isDamaged();
 }
}
