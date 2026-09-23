package dev.apocalypse.pp.mixin;
import dev.apocalypse.pp.ApocalypsePP;
import net.mcreator.protectionpixel.init.ProtectionPixelModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
/** Native exhausted-plate recovery uses Alloy's 15% loss instead of fallback 25%. */
@Mixin(targets={"net.mcreator.protectionpixel.procedures.Deletevalue0Procedure","net.mcreator.protectionpixel.procedures.Deletevalue1Procedure","net.mcreator.protectionpixel.procedures.Deletevalue2Procedure","net.mcreator.protectionpixel.procedures.Deletevalue3Procedure"},remap=false)
public abstract class EchoPlateRecoveryMixin {
 @Redirect(method="execute",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"),remap=false)
 private static Item echoRecoveryTier(ItemStack stack) {
  return stack.is(ApocalypsePP.ECHO_PLATE.get()) ? ProtectionPixelModItems.ALLOYARMORPLATE.get() : stack.getItem();
 }
}
