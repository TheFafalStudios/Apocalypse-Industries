package dev.apocalypse.submarinelava;
import dev.apocalypse.submarinelava.mixin.HullAccess;
/** Called by Iris only while binding a program that consumes the lava mask. */
public final class LavaTexture {
    public static int get() {
        int id = LavaVisuals.active ? HullAccess.ai$hullDepth() : 0;
        return id != 0 ? id : HullAccess.ai$outside().getId();
    }
}
