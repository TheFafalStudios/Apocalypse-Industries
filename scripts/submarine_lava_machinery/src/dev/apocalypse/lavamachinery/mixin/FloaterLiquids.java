package dev.apocalypse.lavamachinery.mixin;
import dev.apocalypse.lavamachinery.Fluids;
import com.maxenonyme.createsubmarine.submarine.block.entity.FloaterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.*;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=FloaterBlockEntity.class,remap=false)
public abstract class FloaterLiquids {
    @Shadow private long cachedSurfaceUntil;
    @Shadow private Vector3d recordedForceVec;
    @Unique private Fluid ai$previousMedium;
    @Inject(method="serverTick",at=@At("HEAD"))
    private static void ai$transition(Level level,BlockPos pos,FloaterBlockEntity be,CallbackInfo ci){
        var self=(FloaterLiquids)(Object)be;
        var sample=Fluids.outside(level,pos);
        var fluid=(sample.isEmpty()?Fluids.outside(level,pos.below()):sample).getType();
        if(fluid!=self.ai$previousMedium){self.cachedSurfaceUntil=Long.MIN_VALUE;self.ai$previousMedium=fluid;}
        self.recordedForceVec=null;
    }
    @Redirect(method={"serverTick","countWaterAbove"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"),require=3)
    private static boolean ai$liquid(FluidState state,TagKey<Fluid> tag){return Fluids.supported(state);}
}
