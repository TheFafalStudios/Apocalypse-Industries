package com.rafal.apocalypsegunsmithing;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

public final class ArmorPenetration {
    public static final float ARMOR_REMAINING = 0.35F;
    private ArmorPenetration() {}

    public static DamageSource mark(ResourceLocation ammo, DamageSource original) {
        if (ammo == null || original == null) return original;
        if (!ammo.getNamespace().equals("cgs")) return original;
        if (!ammo.getPath().equals("round_revolver_piercing")
                && !ammo.getPath().equals("round_gatling_piercing")) return original;
        return new PiercingDamageSource(original);
    }

    public static float effectiveArmor(DamageSource source, float armor) {
        return source instanceof PiercingDamageSource ? armor * ARMOR_REMAINING : armor;
    }
}
