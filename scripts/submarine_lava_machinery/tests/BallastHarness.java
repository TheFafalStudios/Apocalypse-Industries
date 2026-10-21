package dev.apocalypse.lavamachinery.tests;
import dev.apocalypse.lavamachinery.*;
import com.maxenonyme.createsubmarine.submarine.block.entity.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.nio.file.*;
import java.util.*;
@Mod("apocalypse_ballast_tests")
public final class BallastHarness {
    private static int checks;
    private static final Fluid WATER=net.minecraft.world.level.material.Fluids.WATER,LAVA=net.minecraft.world.level.material.Fluids.LAVA;
    private static void check(boolean condition,String label){checks++;if(!condition)throw new AssertionError(label);}
    public static FluidStack stack(Fluid fluid,int amount){return new FluidStack(fluid,amount);}
    private static final class Vent implements VentFluid {
        FluidStack ocean=FluidStack.EMPTY;int bubbles;
        public FluidStack ai$ocean(){return ocean.copy();}
        public void ai$bubble(boolean intake){bubbles++;}
    }
    public BallastHarness(IEventBus bus){bus.addListener(this::loaded);}
    private void loaded(FMLLoadCompleteEvent event){event.enqueueWork(()->{
        try {
            if(!Boolean.getBoolean("apocalypse.ballast.baseline"))run();
            Files.writeString(Path.of("ballast-native-validation.json"),new com.google.gson.Gson().toJson(Map.of("pass",true,"checks",checks,"worldStarted",false,"propulsionChanged",false)));
            if(Boolean.getBoolean("apocalypse.ballast.worldTest"))WorldHarness.arm();else System.exit(0);
        } catch(Throwable error){fail(error);}
    });}
    public static void fail(Throwable error){
        error.printStackTrace();
        try{Files.writeString(Path.of("ballast-test-failure.json"),new com.google.gson.Gson().toJson(Map.of("pass",false,"checks",checks,"error",error.toString())));}catch(Exception ignored){}
        System.exit(1);
    }
    public static void run() throws Exception {
        for(var fluid:List.of(WATER,LAVA))for(int amount:new int[]{0,1,80,81,82,7999,8000,8001,15999,16000,16001}) {
            var a=new FluidTank(8000);var b=new FluidTank(8000);var tanks=List.of(a,b);
            int expected=Math.min(amount,16000);
            check(ClusterFluids.fill(tanks,stack(fluid,amount),FluidAction.SIMULATE)==expected,"simulated capacity");
            check(a.isEmpty()&&b.isEmpty(),"simulation must not mutate");
            check(ClusterFluids.fill(tanks,stack(fluid,amount),FluidAction.EXECUTE)==expected,"executed capacity");
            var current=ClusterFluids.contents(tanks);
            check(current.getAmount()==expected,"reported conserved amount");
            check(expected==0||current.getFluid()==fluid,"reported fluid identity");
            var other=fluid==WATER?LAVA:WATER;
            if(expected>0)check(ClusterFluids.fill(tanks,stack(other,100),FluidAction.EXECUTE)==0,"reject mixed cluster fill");
            check(ClusterFluids.drain(tanks,stack(other,100),FluidAction.EXECUTE).isEmpty(),"reject wrong-fluid drain");
            var simulated=ClusterFluids.drain(tanks,Integer.MAX_VALUE,FluidAction.SIMULATE);
            check(simulated.getAmount()==expected,"simulated drain amount");
            check(a.getFluidAmount()+b.getFluidAmount()==expected,"drain simulation unchanged");
            var removed=ClusterFluids.drain(tanks,Integer.MAX_VALUE,FluidAction.EXECUTE);
            check(removed.getAmount()==expected,"actual drain conserved");
            check(expected==0||removed.getFluid()==fluid,"drained actual fluid");
            check(a.isEmpty()&&b.isEmpty(),"drain completely empty");
        }
        for(var fluid:List.of(WATER,LAVA)) {
            var a=new FluidTank(8000);var b=new FluidTank(8000);a.fill(stack(fluid,7000),FluidAction.EXECUTE);
            check(ClusterFluids.transfer(a,b,3500)==3500,"equalization transfer");
            check(a.getFluidAmount()==3500&&b.getFluidAmount()==3500,"equalization amount");
            b.setFluid(stack(fluid==WATER?LAVA:WATER,100));
            check(ClusterFluids.transfer(a,b,1000)==0,"incompatible transfer rejected");
            check(a.getFluidAmount()==3500&&b.getFluidAmount()==100,"no fluid loss on rejection");
        }
        var a=new FluidTank(8000);var b=new FluidTank(8000);
        a.setFluid(stack(WATER,400));b.setFluid(stack(LAVA,600));
        var mixed=List.of(a,b);
        check(ClusterFluids.contents(mixed).getAmount()==400,"legacy mixture reports one real fluid");
        check(ClusterFluids.drain(mixed,1000,FluidAction.EXECUTE).getFluid()==WATER,"legacy drain water first");
        var lava=ClusterFluids.drain(mixed,1000,FluidAction.EXECUTE);
        check(lava.getFluid()==LAVA&&lava.getAmount()==600,"legacy lava preserved");
        var vent=new Vent();var sea=new SeaHandler(vent);
        check(sea.getFluidInTank(0).isEmpty()&&sea.drain(100,FluidAction.EXECUTE).isEmpty(),"dry vent cannot source fluid");
        for(var fluid:List.of(WATER,LAVA)) {
            vent.ocean=stack(fluid,1);var other=fluid==WATER?LAVA:WATER;
            check(sea.getFluidInTank(0).getFluid()==fluid,"ocean fluid follows transition immediately");
            var intake=sea.drain(250,FluidAction.SIMULATE);
            check(intake.getFluid()==fluid&&intake.getAmount()==250,"simulated vent intake identity");
            check(sea.drain(stack(other,250),FluidAction.EXECUTE).isEmpty(),"vent cannot manufacture requested wrong fluid");
            check(sea.fill(stack(other,250),FluidAction.EXECUTE)==250,"old ballast may discharge across fluid transition");
        }
        vent.ocean=FluidStack.EMPTY;
        check(sea.fill(stack(LAVA,100),FluidAction.EXECUTE)==0,"dry discharge retains upstream submerged requirement");
        var lookup=RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        for(var fluid:List.of(WATER,LAVA)) {
            var block=(EntityBlock)BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create_submarine:ballast_tank"));
            var state=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create_submarine:ballast_tank")).defaultBlockState();
            var be=(BallastTankBlockEntity)block.newBlockEntity(BlockPos.ZERO,state);
            var handler=be.getClusterFluidHandler(Direction.DOWN);
            check(handler instanceof ClusterHandler,"native cluster handler replacement");
            check(handler.fill(stack(fluid,200),FluidAction.EXECUTE)==81,"normal pipe rate preserved");
            check(handler.getFluidInTank(0).getFluid()==fluid,"native handler reports fluid");
            check(handler.drain(stack(fluid==WATER?LAVA:WATER,80),FluidAction.EXECUTE).isEmpty(),"native wrong-fluid request rejected");
            var saved=be.saveWithFullMetadata(lookup);
            var restored=(BallastTankBlockEntity)block.newBlockEntity(BlockPos.ZERO,state);
            restored.loadWithComponents(saved,lookup);
            check(restored.tank.getFluid().getFluid()==fluid&&restored.tank.getFluidAmount()==81,"native tank save/load identity");
            check(handler.drain(200,FluidAction.EXECUTE).getAmount()==81,"native handler drains stored fluid");
            check(be.fillCluster(100,FluidAction.EXECUTE)==0,"direct fill without wet vent cannot manufacture water");
        }
        for(var cls:List.of(BallastTankBlockEntity.class,FloaterBlockEntity.class)) {
            var method=Arrays.stream(cls.getDeclaredMethods()).filter(m->m.getName().contains("ai$liquid")).findFirst().orElseThrow();
            method.setAccessible(true);
            check((boolean)method.invoke(null,WATER.defaultFluidState(),FluidTags.WATER),"water force gate");
            check((boolean)method.invoke(null,LAVA.defaultFluidState(),FluidTags.WATER),"lava force gate");
            check(!(boolean)method.invoke(null,net.minecraft.world.level.material.Fluids.EMPTY.defaultFluidState(),FluidTags.WATER),"air force gate");
        }
        check(VentFluid.class.isAssignableFrom(BallastVentBlockEntity.class),"vent fluid bridge applied");
        System.out.println("Ballast runtime checks passed: "+checks);
    }
}
