package dev.apocalypse.combat.mixin;
import dev.apocalypse.combat.CombatScope;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=WeaponRegistry.class, remap=false)
public abstract class WeaponRegistryMixin {
 @Inject(method="register", at=@At("HEAD"), cancellable=true)
 private static void scopeRegistration(ResourceLocation id, WeaponAttributes attributes, CallbackInfo ci) {
  if(!CombatScope.allows(BuiltInRegistries.ITEM.get(id).getDefaultInstance())) ci.cancel();
 }
 @Inject(method="getAttributes(Lnet/minecraft/world/item/ItemStack;)Lnet/bettercombat/api/WeaponAttributes;", at=@At("HEAD"), cancellable=true)
 private static void scopeLookup(ItemStack stack, CallbackInfoReturnable<WeaponAttributes> cir) {
  if(!CombatScope.allows(stack)) cir.setReturnValue(null);
 }
}
