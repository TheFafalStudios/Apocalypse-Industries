package dev.apocalypse.lavamachinery.mixin;
import dev.apocalypse.lavamachinery.*;
import com.maxenonyme.createsubmarine.submarine.block.entity.BallastVentBlockEntity;
import net.minecraft.core.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.*;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import java.util.List;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=BallastVentBlockEntity.class,remap=false)
public abstract class VentLiquids implements VentFluid {
    @Shadow private List<Direction> getOpenFaces(){throw new AssertionError();}
    @Shadow private void bubble(boolean intake){throw new AssertionError();}
    @Unique private IFluidHandler ai$handler;
    public FluidStack ai$ocean(){
        BlockEntity be=(BlockEntity)(Object)this;
        for(var face:getOpenFaces()) {
            var fluid=dev.apocalypse.lavamachinery.Fluids.outside(be.getLevel(),be.getBlockPos().relative(face));
            if(dev.apocalypse.lavamachinery.Fluids.supported(fluid))
                return new FluidStack(dev.apocalypse.lavamachinery.Fluids.source(fluid.getType()),1);
        }
        return FluidStack.EMPTY;
    }
    public void ai$bubble(boolean intake){bubble(intake);}
    @Inject(method="getFluidHandlerForSide",at=@At("HEAD"),cancellable=true)
    private void ai$handler(Direction side,CallbackInfoReturnable<IFluidHandler> cir){
        if(ai$handler==null)ai$handler=new SeaHandler(this);
        cir.setReturnValue(ai$handler);
    }
    @Redirect(method="isSubmerged",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"),require=2)
    private boolean ai$liquid(FluidState state,TagKey<Fluid> tag){return dev.apocalypse.lavamachinery.Fluids.supported(state);}
}
