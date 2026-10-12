package dev.apocalypse.studious.mixin;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Apply after Studious's Fabric injection; NeoForge already handles shared keys. */
@Mixin(value=KeyMapping.class,priority=500)
public abstract class NativeSharedKeys {
    @Unique private static final boolean apocalypse$usesNativeSharedKeys=true;
}
