package dev.apocalypse.railways;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Defer manual phantom uploads until reload workers finish building mip chains. */
@Mixin(targets="com.railwayteam.railways.content.custom_tracks.phantom.PhantomSpriteManager", remap=false)
public abstract class PhantomReloadGuard {
    @Inject(method="renderTick()V", at=@At("HEAD"), cancellable=true, remap=false)
    private static void apocalypse$deferDuringReload(CallbackInfo ci) {
        if (Minecraft.getInstance().getOverlay() instanceof LoadingOverlay) {
            // Preserve hasChanged so visibility is uploaded after loading completes.
            ci.cancel();
        }
    }
}
