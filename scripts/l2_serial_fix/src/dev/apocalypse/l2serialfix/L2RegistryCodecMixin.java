package dev.apocalypse.l2serialfix;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
/** Match the existing dedicated server's name/NBT encoding without changing registry sync flags. */
@Mixin(targets="dev.xkmc.l2serial.serialization.custom_handler.Handlers", remap=false)
public abstract class L2RegistryCodecMixin {
    @Redirect(method="enableVanilla", at=@At(value="INVOKE", target="Lnet/neoforged/neoforge/registries/RegistryManager;isNonSyncedBuiltInRegistry(Lnet/minecraft/core/Registry;)Z"), remap=false, require=1, allow=1)
    private static boolean apocalypse$useNameEncoding(Registry<?> registry) {
        return true;
    }
}
