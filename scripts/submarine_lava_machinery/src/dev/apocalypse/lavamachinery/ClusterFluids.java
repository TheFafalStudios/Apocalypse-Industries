package dev.apocalypse.lavamachinery;
import java.util.List;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
/** Single-fluid logical cluster. Legacy mixed tanks can drain without conversion or loss. */
public final class ClusterFluids {
    public static FluidStack contents(List<FluidTank> tanks) {
        FluidStack first=FluidStack.EMPTY; int amount=0;
        for(var tank:tanks) {
            var stack=tank.getFluid(); if(stack.isEmpty())continue;
            if(first.isEmpty())first=stack;
            if(FluidStack.isSameFluidSameComponents(first,stack))amount=Math.addExact(amount,stack.getAmount());
        }
        return first.isEmpty()?FluidStack.EMPTY:first.copyWithAmount(amount);
    }
    public static int fill(List<FluidTank> tanks,FluidStack input,FluidAction action) {
        if(!Fluids.supported(input))return 0;
        for(var tank:tanks)
            if(!tank.isEmpty() && !FluidStack.isSameFluidSameComponents(tank.getFluid(),input))return 0;
        int remaining=input.getAmount();
        for(var tank:tanks) {
            remaining-=tank.fill(input.copyWithAmount(remaining),action);
            if(remaining<=0)break;
        }
        return input.getAmount()-remaining;
    }
    public static FluidStack drain(List<FluidTank> tanks,FluidStack requested,FluidAction action) {
        if(requested.isEmpty())return FluidStack.EMPTY;
        var current=contents(tanks);
        if(current.isEmpty() || !FluidStack.isSameFluidSameComponents(current,requested))return FluidStack.EMPTY;
        int remaining=requested.getAmount();
        for(var tank:tanks) {
            var removed=tank.drain(requested.copyWithAmount(remaining),action);remaining-=removed.getAmount();
            if(remaining<=0)break;
        }
        int total=requested.getAmount()-remaining;
        return total==0?FluidStack.EMPTY:requested.copyWithAmount(total);
    }
    public static FluidStack drain(List<FluidTank> tanks,int amount,FluidAction action) {
        var current=contents(tanks);
        return amount<=0 || current.isEmpty()?FluidStack.EMPTY:drain(tanks,current.copyWithAmount(amount),action);
    }
    public static int transfer(FluidTank from,FluidTank to,int amount) {
        if(amount<=0)return 0;
        var offered=from.drain(amount,FluidAction.SIMULATE);
        int accepted=to.fill(offered,FluidAction.SIMULATE);if(accepted<=0)return 0;
        var removed=from.drain(accepted,FluidAction.EXECUTE);
        int inserted=to.fill(removed,FluidAction.EXECUTE);
        if(inserted<removed.getAmount())from.fill(removed.copyWithAmount(removed.getAmount()-inserted),FluidAction.EXECUTE);
        return inserted;
    }
}
