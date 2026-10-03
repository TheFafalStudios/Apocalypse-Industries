package com.rafal.apocalypsegunsmithing.mixin;

import com.rafal.apocalypsegunsmithing.ArmorPenetration;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = CombatRules.class, remap = false)
public abstract class CombatRulesMixin {
    @ModifyVariable(method = "getDamageAfterAbsorb(Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/damagesource/DamageSource;FF)F",
            at = @At("HEAD"), argsOnly = true, index = 3, remap = false)
    private static float apocalypseGunsmithing$reduceArmor(float armor, LivingEntity target,
            float damage, DamageSource source, float originalArmor, float toughness) {
        return ArmorPenetration.effectiveArmor(source, armor);
    }
}
