package dev.apocalypse.lavamachinery.tests.trace;
import dev.ryanhcode.sable.sublevel.SubLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=SubLevel.class,remap=false)
public abstract class MarkTrace {
    @Inject(method="markRemoved",at=@At("HEAD"))
    private void trace(CallbackInfo ci){new Exception("Fixture sublevel marked removed").printStackTrace();}
}
