package com.rafal.apocalypsegunsmithing.mixin;

import com.rafal.apocalypsegunsmithing.GunTypes;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.nukateam.ntgl.common.foundation.item.WeaponItem", remap = false)
public abstract class WeaponItemMixin {
    @Inject(
            method = "getEnchantmentValue(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void apocalypseGunsmithing$enchantmentValue(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (GunTypes.isEnchantableCgsGun(this)) cir.setReturnValue(5);
    }

    @Inject(
            method = "getEnchantmentValue()I",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void apocalypseGunsmithing$enchantmentValueLegacy(CallbackInfoReturnable<Integer> cir) {
        if (GunTypes.isEnchantableCgsGun(this)) cir.setReturnValue(5);
    }

    @Inject(
            method = "isBookEnchantable(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void apocalypseGunsmithing$bookEnchantable(ItemStack stack, ItemStack book, CallbackInfoReturnable<Boolean> cir) {
        if (GunTypes.isEnchantableCgsGun(this)) cir.setReturnValue(true);
    }
}
