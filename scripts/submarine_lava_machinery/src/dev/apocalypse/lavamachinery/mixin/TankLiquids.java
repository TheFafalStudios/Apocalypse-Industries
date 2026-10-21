package dev.apocalypse.lavamachinery.mixin;
import dev.apocalypse.lavamachinery.*;
import com.maxenonyme.createsubmarine.submarine.block.entity.BallastTankBlockEntity;
import net.minecraft.core.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.List;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=BallastTankBlockEntity.class,remap=false)
public abstract class TankLiquids {
    @Shadow @Final public FluidTank tank;
    @Shadow private Vector3d recordedForceVec;
    @Shadow private List<BallastTankBlockEntity> getCluster(){throw new AssertionError();}
    @Inject(method="<init>",at=@At("RETURN"))
    private void ai$validator(BlockPos pos,BlockState state,CallbackInfo ci){tank.setValidator(dev.apocalypse.lavamachinery.Fluids::supported);}
    @Redirect(method={"serverTick","countWaterAbove"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"),require=3)
    private static boolean ai$liquid(FluidState state,TagKey<Fluid> tag){return dev.apocalypse.lavamachinery.Fluids.supported(state);}
    @Inject(method="serverTick",at=@At("HEAD"))
    private static void ai$clearForce(Level level,BlockPos pos,BallastTankBlockEntity be,CallbackInfo ci){
        ((TankLiquids)(Object)be).recordedForceVec=null;
    }
    @Inject(method="getClusterFluidHandler",at=@At("HEAD"),cancellable=true)
    private void ai$handler(Direction side,CallbackInfoReturnable<IFluidHandler> cir){cir.setReturnValue(new ClusterHandler((BallastTankBlockEntity)(Object)this,side));}
    @Inject(method="fillCluster",at=@At("HEAD"),cancellable=true)
    private void ai$fill(int amount,FluidAction action,CallbackInfoReturnable<Integer> cir){
        var cluster=getCluster();int filled=0;
        if(amount>0)outer:for(var member:cluster) {
            var level=member.getLevel();if(level==null)continue;
            for(var side:Direction.values()) {
                var be=level.getBlockEntity(member.getBlockPos().relative(side));
                if(be instanceof VentFluid vent) {
                    var ocean=vent.ai$ocean();
                    if(!ocean.isEmpty()) {
                        filled=ClusterFluids.fill(cluster.stream().map(t->t.tank).toList(),ocean.copyWithAmount(amount),action);
                        break outer;
                    }
                }
            }
        }
        cir.setReturnValue(filled);
    }
    @Inject(method="drainCluster",at=@At("HEAD"),cancellable=true)
    private void ai$drain(int amount,FluidAction action,CallbackInfoReturnable<Integer> cir){
        cir.setReturnValue(ClusterFluids.drain(getCluster().stream().map(t->t.tank).toList(),amount,action).getAmount());
    }
    @Inject(method="shareFluidWithNeighbors",at=@At("HEAD"),cancellable=true)
    private void ai$share(CallbackInfo ci){
        ci.cancel();var self=(BallastTankBlockEntity)(Object)this;var level=self.getLevel();
        if(level==null || level.isClientSide)return;
        for(var side:Direction.values()) {
            var be=level.getBlockEntity(self.getBlockPos().relative(side));
            if(be instanceof BallastTankBlockEntity other) {
                int difference=tank.getFluidAmount()-other.tank.getFluidAmount();
                if(difference>2)ClusterFluids.transfer(tank,other.tank,difference/2);
            }
        }
    }
    @ModifyArg(method="addToGoggleTooltip",at=@At(value="INVOKE",target="Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"),index=0)
    private String ai$label(String key){return key.equals("create_submarine.gui.goggles.water")?"apocalypse_submarine_lava_machinery.ballast_fluid":key;}
}
