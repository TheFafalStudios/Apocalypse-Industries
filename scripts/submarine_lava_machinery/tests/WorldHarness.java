package dev.apocalypse.lavamachinery.tests;
import dev.apocalypse.lavamachinery.*;
import com.maxenonyme.createsubmarine.submarine.block.entity.*;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.joml.Vector3d;
import java.nio.file.*;
import java.util.*;

/** Construct fresh disposable pools and real Sable assemblies, using no user schematic/save. */
public final class WorldHarness {
    private static int ticks;
    private static final boolean BASELINE=Boolean.getBoolean("apocalypse.ballast.baseline");
    private static final List<Map<String,Object>> ROWS=new ArrayList<>();
    private record Scene(ServerSubLevel sub,BallastTankBlockEntity tank,BallastVentBlockEntity vent){}
    private static final Map<Fluid,Scene> SCENES=new HashMap<>();
    private static boolean preparing;
    private static void check(boolean result,String label){if(!result)throw new AssertionError(label);}
    public static void arm(){NeoForge.EVENT_BUS.addListener(WorldHarness::tick);}
    private static void tick(ServerTickEvent.Post event){
        ++ticks;
        if(ticks!=5&&ticks!=10)return;
        try {
            var level=event.getServer().overworld();
            preparing=ticks==5;
            run(level,new BlockPos(8,150,8),net.minecraft.world.level.material.Fluids.WATER);
            run(level,new BlockPos(40,150,8),net.minecraft.world.level.material.Fluids.LAVA);
            if(preparing)return; // Let Sable publish its containing-plot and physics-body registration.
            Files.writeString(Path.of("ballast-world-validation.json"),new com.google.gson.Gson().toJson(Map.of("pass",true,"method","ballast-native-v1","baseline",BASELINE,"assemblies",ROWS,"propulsionChanged",false,"world","fresh isolated ballast-audit-world")));
            // Only this test process exits; normal WIP/server processes are never accessed.
            System.exit(0);
        }catch(Throwable error){BallastHarness.fail(error);}
    }
    private static void pool(ServerLevel level,BlockPos origin,Fluid fluid){
        var liquid=fluid.defaultFluidState().createLegacyBlock();
        for(var p:BlockPos.betweenClosed(origin.offset(-3,-5,-3),origin.offset(4,5,3))) {
            boolean edge=p.getX()==origin.getX()-3||p.getX()==origin.getX()+4||p.getZ()==origin.getZ()-3||p.getZ()==origin.getZ()+3||p.getY()==origin.getY()-5;
            level.setBlock(p,edge?Blocks.BARRIER.defaultBlockState():liquid,2);
        }
    }
    private static double force(BallastTankBlockEntity tank,Fluid fluid,int amount) throws Exception {
        tank.tank.setFluid(amount==0?FluidStack.EMPTY:new FluidStack(fluid,amount));
        BallastTankBlockEntity.serverTick(tank.getLevel(),tank.getBlockPos(),tank);
        var field=BallastTankBlockEntity.class.getDeclaredField("recordedForceVec");field.setAccessible(true);
        var force=(Vector3d)field.get(tank);return force==null?0:force.y;
    }
    private static void run(ServerLevel level,BlockPos origin,Fluid fluid) throws Exception {
        if(preparing) {
        pool(level,origin,fluid);
        var tankBlock=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create_submarine:ballast_tank"));
        var ventBlock=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create_submarine:ballast_vent"));
        level.setBlock(origin,tankBlock.defaultBlockState(),3);level.setBlock(origin.east(),ventBlock.defaultBlockState(),3);
        level.setBlock(origin.below(),Blocks.NETHERITE_BLOCK.defaultBlockState(),3);
        level.setBlock(origin.east().below(),Blocks.NETHERITE_BLOCK.defaultBlockState(),3);
        ServerSubLevel sub=SubLevelAssemblyHelper.assembleBlocks(level,origin,List.of(origin,origin.east(),origin.below(),origin.east().below()),new BoundingBox3i(origin.getX(),origin.getY()-1,origin.getZ(),origin.getX()+2,origin.getY()+1,origin.getZ()+1));
        dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level).addForceLoadTicket(sub,
            dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType.COMMAND_FORCED,net.minecraft.util.Unit.INSTANCE);
        // Restore ocean fluid in the cells vacated by assembly, before any physics test.
        level.setBlock(origin,fluid.defaultFluidState().createLegacyBlock(),2);level.setBlock(origin.east(),fluid.defaultFluidState().createLegacyBlock(),2);
        BallastTankBlockEntity tank=null;BallastVentBlockEntity vent=null;
        var bounds=sub.getPlot().getBoundingBox();
        for(var pos:BlockPos.betweenClosed(new BlockPos(bounds.minX(),bounds.minY(),bounds.minZ()),new BlockPos(bounds.maxX(),bounds.maxY(),bounds.maxZ()))) {
            var be=level.getBlockEntity(pos);
            if(be instanceof BallastTankBlockEntity t)tank=t;
            if(be instanceof BallastVentBlockEntity v)vent=v;
        }
        check(tank!=null&&vent!=null,"Assembled tank and vent must exist");
        SCENES.put(fluid,new Scene(sub,tank,vent));return;
        }
        var scene=SCENES.get(fluid);var tank=scene.tank();var vent=scene.vent();var sub=scene.sub();
        System.out.println("Sable fixture context: provider="+dev.ryanhcode.sable.companion.SableCompanion.INSTANCE.getClass()+", removed="+sub.isRemoved()+", byId="+dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level).getSubLevel(sub.getUniqueId())+", directContaining="+dev.ryanhcode.sable.Sable.HELPER.getContaining(level,tank.getBlockPos()));
        var point=new Vector3d(tank.getBlockPos().getX()+.5,tank.getBlockPos().getY()+.5,tank.getBlockPos().getZ()+.5);
        sub.logicalPose().transformPosition(point);
        var sampled=BlockPos.containing(point.x,point.y,point.z);
        System.out.println("Ballast scene: "+fluid+", plot="+tank.getBlockPos()+", pose="+sub.logicalPose().position()+", pivot="+sub.logicalPose().rotationPoint()+", transformed="+point+", directFluid="+level.getFluidState(sampled)+", originFluid="+level.getFluidState(origin)+", level="+tank.getLevel().getClass()+", containing="+dev.ryanhcode.sable.companion.SableCompanion.INSTANCE.getContaining(tank.getLevel(),tank.getBlockPos())+", outside="+dev.apocalypse.lavamachinery.Fluids.outside(tank.getLevel(),tank.getBlockPos())+", mass="+com.maxenonyme.createsubmarine.submarine.util.SablePhysicsHelper.readMass(sub));
        var velocity=com.maxenonyme.createsubmarine.submarine.util.SablePhysicsHelper.getVelocity(sub);
        double initialVelocityY=velocity==null?0:velocity.y();
        double empty=force(tank,fluid,0),full=force(tank,fluid,8000);
        boolean water=fluid==net.minecraft.world.level.material.Fluids.WATER;
        check(Double.isFinite(empty)&&Double.isFinite(full),"Finite ballast forces");
        if(BASELINE&&!water)check(empty==0&&full==0,"Negative control must reproduce missing lava force");
        else check((empty>0||initialVelocityY>0)&&full<0&&full<empty,"Empty ballast must rise or maintain ascent; full ballast must command descent in "+fluid+": "+empty+","+full+", velocity="+initialVelocityY);
        vent.tick();var ocean=vent.getFluidHandlerForSide(Direction.UP);
        var source=ocean.drain(8000,FluidAction.SIMULATE);
        if(!BASELINE) {
            check(source.getFluid()==fluid&&source.getAmount()==8000,"Assembled wet vent exposes actual ocean");
            tank.tank.drain(Integer.MAX_VALUE,FluidAction.EXECUTE);
            var handler=tank.getClusterFluidHandler(Direction.EAST);
            int filled=handler.fill(source,FluidAction.EXECUTE);
            check(filled>0&&tank.tank.getFluid().getFluid()==fluid,"Assembled pipe-to-ballast intake identity");
            var removed=handler.drain(Integer.MAX_VALUE,FluidAction.EXECUTE);
            check(removed.getFluid()==fluid&&removed.getAmount()==filled,"Assembled drain identity and amount");
        }
        ROWS.add(Map.of("fluid",water?"water":"lava","initialVelocityY",initialVelocityY,"emptyTankForceY",empty,"fullTankForceY",full,"ventFluid",source.isEmpty()?"empty":source.getFluid().toString(),"assembled",true));
    }
}
