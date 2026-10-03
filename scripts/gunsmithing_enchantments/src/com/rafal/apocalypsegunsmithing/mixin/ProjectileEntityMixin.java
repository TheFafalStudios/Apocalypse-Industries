package com.rafal.apocalypsegunsmithing.mixin;

import com.rafal.apocalypsegunsmithing.ArmorPenetration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.nukateam.ntgl.common.foundation.entity.ProjectileEntity", remap = false)
public abstract class ProjectileEntityMixin {
    @Shadow(remap = false) public abstract ItemStack getItem();

    @Inject(method = "getDamageSource()Lnet/minecraft/world/damagesource/DamageSource;",
            at = @At("RETURN"), cancellable = true, remap = false)
    private void apocalypseGunsmithing$markPiercing(CallbackInfoReturnable<DamageSource> cir) {
        ItemStack ammo = getItem();
        if (ammo.isEmpty()) return;
        DamageSource original = cir.getReturnValue();
        DamageSource marked = ArmorPenetration.mark(BuiltInRegistries.ITEM.getKey(ammo.getItem()), original);
        if (marked != original) cir.setReturnValue(marked);
    }
}
