package dev.apocalypse.pp.mixin;
import dev.apocalypse.pp.ConsumableRepairRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=ItemStack.class,remap=false)
public abstract class ConsumableEnchantmentsMixin {
 @Inject(method="set",at=@At("HEAD"),cancellable=true)
 @SuppressWarnings("unchecked")
 private void filterWrite(DataComponentType<?> type,Object value,CallbackInfoReturnable<Object> cir) {
  if((type==DataComponents.ENCHANTMENTS || type==DataComponents.STORED_ENCHANTMENTS) && value instanceof ItemEnchantments original) {
   ItemStack stack=(ItemStack)(Object)this;
   var clean=ConsumableRepairRules.filter(stack,original);
   if(clean!=original)cir.setReturnValue(stack.set((DataComponentType<ItemEnchantments>)type,clean));
  }
 }
 // Loaded stacks can bypass set(); filter before vanilla or modded Mending sees them.
 @Inject(method={"getEnchantments","getTagEnchantments"},at=@At("RETURN"),cancellable=true)
 private void filterRead(CallbackInfoReturnable<ItemEnchantments> cir) {
  ItemStack stack=(ItemStack)(Object)this;
  var clean=ConsumableRepairRules.filter(stack,cir.getReturnValue());
  if(clean!=cir.getReturnValue()) {
   stack.set(DataComponents.ENCHANTMENTS,clean);
   cir.setReturnValue(clean);
  }
 }
}
