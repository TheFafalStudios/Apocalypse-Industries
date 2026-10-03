package com.rafal.apocalypsegunsmithing.mixin;

import com.rafal.apocalypsegunsmithing.GunTypes;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.nukateam.cgs.common.faundation.item.guns.CgsGunItem", remap = false)
public abstract class CgsGunItemMixin {
    @Inject(
            method = "isEnchantable(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void apocalypseGunsmithing$enableEnchanting(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (GunTypes.isEnchantableCgsGun(this)) {
            cir.setReturnValue(true);
        }
    }
}
