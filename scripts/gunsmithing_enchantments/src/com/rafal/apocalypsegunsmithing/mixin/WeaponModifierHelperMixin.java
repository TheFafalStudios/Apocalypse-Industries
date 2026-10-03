package com.rafal.apocalypsegunsmithing.mixin;

import com.nukateam.ntgl.common.data.WeaponData;
import com.rafal.apocalypsegunsmithing.EnchantmentLevels;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.nukateam.ntgl.common.util.util.WeaponModifierHelper", remap = false)
public abstract class WeaponModifierHelperMixin {
    @Inject(method = "getReloadStart(Lcom/nukateam/ntgl/common/data/WeaponData;)I", at = @At("RETURN"), cancellable = true, remap = false)
    private static void apocalypseGunsmithing$quickHandsStart(WeaponData data, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(applyQuickHands(data, cir.getReturnValue()));
    }

    @Inject(method = "getReloadTime(Lcom/nukateam/ntgl/common/data/WeaponData;)I", at = @At("RETURN"), cancellable = true, remap = false)
    private static void apocalypseGunsmithing$quickHandsMain(WeaponData data, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(applyQuickHands(data, cir.getReturnValue()));
    }

    @Inject(method = "getReloadEnd(Lcom/nukateam/ntgl/common/data/WeaponData;)I", at = @At("RETURN"), cancellable = true, remap = false)
    private static void apocalypseGunsmithing$quickHandsEnd(WeaponData data, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(applyQuickHands(data, cir.getReturnValue()));
    }

    // getRate is the actual shot cooldown in NTGL 3.1.8 (ShootTracker stores rate * 50 ms).
    @Inject(method = "getRate(Lcom/nukateam/ntgl/common/data/WeaponData;)I", at = @At("RETURN"), cancellable = true, remap = false)
    private static void apocalypseGunsmithing$triggerFinger(WeaponData data, CallbackInfoReturnable<Integer> cir) {
        int level = EnchantmentLevels.get(data.weapon, EnchantmentLevels.TRIGGER_FINGER);
        if (level <= 0) return;
        float factor = Math.max(0.10F, 1.0F - 0.25F * level);
        cir.setReturnValue(Math.max(1, Math.round(cir.getReturnValue() * factor)));
    }

    @Inject(method = "getModifiedAimDownSightSpeed(Lcom/nukateam/ntgl/common/data/WeaponData;)D", at = @At("RETURN"), cancellable = true, remap = false)
    private static void apocalypseGunsmithing$lightweight(WeaponData data, CallbackInfoReturnable<Double> cir) {
        int level = EnchantmentLevels.get(data.weapon, EnchantmentLevels.LIGHTWEIGHT);
        if (level <= 0) return;
        cir.setReturnValue(cir.getReturnValue() * (1.0D + 0.5D * level));
    }

    // RETURN injection makes Sharpshooter multiply the damage after ammo + every NTGL attachment modifier.
    @Inject(method = "getProjectileDamage(Lnet/minecraft/resources/ResourceLocation;Lcom/nukateam/ntgl/common/data/WeaponData;)F", at = @At("RETURN"), cancellable = true, remap = false)
    private static void apocalypseGunsmithing$sharpshooter(ResourceLocation ammo, WeaponData data, CallbackInfoReturnable<Float> cir) {
        int level = EnchantmentLevels.get(data.weapon, EnchantmentLevels.SHARPSHOOTER);
        if (level <= 0) return;
        cir.setReturnValue(cir.getReturnValue() * (1.0F + 0.25F * level));
    }

    private static int applyQuickHands(WeaponData data, int ticks) {
        int level = EnchantmentLevels.get(data.weapon, EnchantmentLevels.QUICK_HANDS);
        if (level <= 0 || ticks <= 0) return ticks;
        // Restores the old NTGL 30%-per-level progression; CEI's +1 level naturally yields III = 10% time.
        float factor = Math.max(0.10F, 1.0F - 0.30F * level);
        return Math.max(1, Math.round(ticks * factor));
    }
}
