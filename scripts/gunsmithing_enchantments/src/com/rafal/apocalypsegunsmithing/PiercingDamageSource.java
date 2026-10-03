package com.rafal.apocalypsegunsmithing;

import net.minecraft.world.damagesource.DamageSource;

/** Per-hit marker: retains the original damage type, attribution and defenses. */
public final class PiercingDamageSource extends DamageSource {
    public PiercingDamageSource(DamageSource original) {
        super(original.typeHolder(), original.getDirectEntity(), original.getEntity());
    }
}
