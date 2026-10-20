package dev.apocalypse.netherislands;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;

final class PortalAudit {
    static void run(ServerLevel nether,ServerLevel overworld,Map<String,Object> report) {
        var cases=new ArrayList<Object>();
        // Clear only bounded arenas inside this disposable test world, so no
        // natural island can hide a broken low-altitude emergency fallback.
        for(int scenario=0;scenario<2;scenario++) {
            int cx=scenario*64;
            var cursor=new BlockPos.MutableBlockPos();
            for(int z=-17;z<=17;z++) for(int x=cx-17;x<=cx+17;x++) for(int y=60;y<320;y++)
                nether.setBlock(cursor.set(x,y,z),(y<100?Blocks.LAVA:Blocks.AIR).defaultBlockState(),2);
            var axis=scenario==0?Direction.Axis.X:Direction.Axis.Z;
            var unsafe=new BlockPos(cx,70,0);
            portal(nether,unsafe,axis);
            if(nether.getPortalForcer().findClosestPortalPosition(unsafe,true,nether.getWorldBorder()).isPresent())
                throw new AssertionError("Submerged existing portal accepted");
            var created=nether.getPortalForcer().createPortal(new BlockPos(cx,20,0),axis).orElseThrow();
            var base=created.minCorner;
            if(base.getY()<104) throw new AssertionError("Low fallback portal: "+base);
            var along=axis==Direction.Axis.X?Direction.EAST:Direction.SOUTH;
            var normal=along.getClockWise();
            for(int w=0;w<2;w++) for(int side=-1;side<=1;side++) {
                var feet=base.relative(along,w).relative(normal,side);
                if(!nether.getBlockState(feet.below()).isSolid()) throw new AssertionError("Unsupported exit");
                for(int y=0;y<3;y++) {
                    var state=nether.getBlockState(feet.above(y));
                    if(!state.getFluidState().isEmpty() || (!state.isAir() && !state.is(Blocks.NETHER_PORTAL)))
                        throw new AssertionError("Blocked/fluid arrival");
                }
            }
            var found=nether.getPortalForcer().findClosestPortalPosition(unsafe,true,nether.getWorldBorder()).orElseThrow();
            if(found.getY()<104) throw new AssertionError("Unsafe portal preferred over safe portal");
            // Placing lava against an otherwise high portal must also exclude it.
            // Only one side of one column is flooded: the other column's POIs
            // must not let nearest-portal selection bypass the hazard.
            nether.setBlock(base.relative(normal),Blocks.LAVA.defaultBlockState(),2);
            if(nether.getPortalForcer().findClosestPortalPosition(unsafe,true,nether.getWorldBorder()).isPresent())
                throw new AssertionError("Portal beside lava accepted");
            cases.add(Map.of("axis",axis.toString(),"fallbackY",base.getY(),"reusedSafePortalY",found.getY()));
        }
        var low=new BlockPos(128,20,128);
        portal(overworld,low,Direction.Axis.X);
        var found=overworld.getPortalForcer().findClosestPortalPosition(low,false,overworld.getWorldBorder()).orElseThrow();
        if(found.getY()!=20) throw new AssertionError("Overworld portal behavior changed");
        report.put("lavaSeaLevel",nether.getChunkSource().getGenerator().getSeaLevel());
        report.put("portalCases",cases);
        report.put("submergedExistingPortalsRejected",true);
        report.put("lavaAdjacentPortalsRejected",true);
        report.put("overworldLowPortalPreserved",true);
    }

    private static void portal(ServerLevel level,BlockPos base,Direction.Axis axis) {
        var along=axis==Direction.Axis.X?Direction.EAST:Direction.SOUTH;
        for(int w=-1;w<=2;w++) for(int h=-1;h<=3;h++) {
            var p=base.relative(along,w).above(h);
            var state=(w==-1 || w==2 || h==-1 || h==3)?Blocks.OBSIDIAN.defaultBlockState():
                Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS,axis);
            level.setBlock(p,state,18);
        }
    }
}
