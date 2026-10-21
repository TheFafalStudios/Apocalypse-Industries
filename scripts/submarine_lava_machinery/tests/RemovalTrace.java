package dev.apocalypse.lavamachinery.tests.trace;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=SubLevelContainer.class,remap=false)
public abstract class RemovalTrace {
    @Inject(method="removeSubLevel(IILdev/ryanhcode/sable/sublevel/storage/SubLevelRemovalReason;)V",at=@At("HEAD"))
    private void trace(int x,int z,SubLevelRemovalReason reason,CallbackInfo ci){
        new Exception("Fixture sublevel removal: "+reason).printStackTrace();
    }
}
