package dev.apocalypse.lavamachinery;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.minecraft.tags.FluidTags;
public final class SeaHandler implements IFluidHandler {
    private final VentFluid vent;
    public SeaHandler(VentFluid vent) {this.vent=vent;}
    public int getTanks(){return 1;}
    public int getTankCapacity(int tank){return 2000000;}
    public FluidStack getFluidInTank(int tank){return tank==0?vent.ai$ocean().copyWithAmount(1000000):FluidStack.EMPTY;}
    public boolean isFluidValid(int tank,FluidStack stack){return tank==0 && Fluids.supported(stack);}
    private void bubble(FluidStack ocean,boolean intake,FluidAction action){
        if(action.execute() && ocean.getFluid().is(FluidTags.WATER))vent.ai$bubble(intake);
    }
    public int fill(FluidStack stack,FluidAction action){
        var ocean=vent.ai$ocean();if(ocean.isEmpty() || !Fluids.supported(stack))return 0;
        // Old water can be discharged into lava, without converting anything inside the tanks.
        bubble(ocean,false,action);return stack.getAmount();
    }
    public FluidStack drain(FluidStack requested,FluidAction action){
        var ocean=vent.ai$ocean();
        if(requested.isEmpty() || ocean.isEmpty() || !FluidStack.isSameFluidSameComponents(ocean,requested))return FluidStack.EMPTY;
        bubble(ocean,true,action);return ocean.copyWithAmount(requested.getAmount());
    }
    public FluidStack drain(int amount,FluidAction action){
        var ocean=vent.ai$ocean();if(amount<=0 || ocean.isEmpty())return FluidStack.EMPTY;
        bubble(ocean,true,action);return ocean.copyWithAmount(amount);
    }
}
