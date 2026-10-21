package dev.apocalypse.lavamachinery;
import com.maxenonyme.createsubmarine.submarine.block.entity.BallastTankBlockEntity;
import dev.apocalypse.lavamachinery.mixin.TankAccess;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.List;
public final class ClusterHandler implements IFluidHandler {
    private final BallastTankBlockEntity root; private final Direction side;
    private long last=Long.MIN_VALUE; private boolean vent;
    public ClusterHandler(BallastTankBlockEntity root,Direction side){this.root=root;this.side=side;}
    public List<FluidTank> tanks(){return ((TankAccess)root).ai$cluster().stream().map(t->t.tank).toList();}
    private int rate(){
        var level=root.getLevel();
        if(level!=null && side!=null && (last==Long.MIN_VALUE || level.getGameTime()-last>=10)) {
            vent=((TankAccess)root).ai$vent(side);last=level.getGameTime();
        }
        return vent?Integer.MAX_VALUE:81;
    }
    public int getTanks(){return 1;}
    public FluidStack getFluidInTank(int tank){return tank==0?ClusterFluids.contents(tanks()):FluidStack.EMPTY;}
    public int getTankCapacity(int tank){return tank==0?tanks().stream().mapToInt(FluidTank::getCapacity).sum():0;}
    public boolean isFluidValid(int tank,FluidStack stack){return tank==0 && Fluids.supported(stack);}
    public int fill(FluidStack stack,FluidAction action){
        if(stack.isEmpty())return 0;
        return ClusterFluids.fill(tanks(),stack.copyWithAmount(Math.min(stack.getAmount(),rate())),action);
    }
    public FluidStack drain(FluidStack stack,FluidAction action){
        return stack.isEmpty()?FluidStack.EMPTY:ClusterFluids.drain(tanks(),stack.copyWithAmount(Math.min(stack.getAmount(),rate())),action);
    }
    public FluidStack drain(int amount,FluidAction action){return ClusterFluids.drain(tanks(),Math.min(amount,rate()),action);}
}
