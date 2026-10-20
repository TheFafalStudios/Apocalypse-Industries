package dev.apocalypse.netherislands;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;

final class SeabedAudit {
    static void run(ServerLevel level,Map<String,Object> report) {
        int[][] heights=new int[128][128]; int[] histogram=new int[6]; int floor=0,maxStep=0;
        for(int cz=-4;cz<4;cz++) for(int cx=-4;cx<4;cx++) {
            var chunk=level.getChunk(cx,cz,ChunkStatus.FEATURES);
            for(int z=0;z<16;z++) for(int x=0;x<16;x++) {
                int wx=cx*16+x,wz=cz*16+z;
                if(!chunk.getBlockState(new BlockPos(wx,-64,wz)).is(Blocks.BEDROCK))
                    throw new AssertionError("Missing bedrock");
                floor++;
                int thickness=0; boolean fluid=false;
                for(int y=-63;y<=-58;y++) {
                    var state=chunk.getBlockState(new BlockPos(wx,y,wz));
                    if(state.is(Blocks.BASALT)) {
                        if(fluid) throw new AssertionError("Gap in seabed");
                        thickness++;
                    } else {
                        if(!state.is(Blocks.LAVA)) throw new AssertionError("Unexpected seabed material at "
                            +wx+","+y+","+wz+": " + state);
                        fluid=true;
                    }
                }
                if(thickness<3 || thickness>5) throw new AssertionError("Wrong thickness " + thickness);
                histogram[thickness]++; heights[wz+64][wx+64]=thickness;
            }
        }
        int transitions=0;
        for(int z=0;z<128;z++) for(int x=0;x<128;x++) {
            if(x>0) {int d=Math.abs(heights[z][x]-heights[z][x-1]);maxStep=Math.max(maxStep,d);if(d>0)transitions++;}
            if(z>0) {int d=Math.abs(heights[z][x]-heights[z-1][x]);maxStep=Math.max(maxStep,d);if(d>0)transitions++;}
        }
        if(histogram[3]==0 || histogram[4]==0 || histogram[5]==0 || maxStep>1)
            throw new AssertionError("Missing variation or abrupt seabed step");
        report.put("chunksChecked",64);report.put("bedrockBlocksChecked",floor);
        report.put("basaltColumnsByThickness",Map.of("3",histogram[3],"4",histogram[4],"5",histogram[5]));
        report.put("maximumAdjacentHeightStep",maxStep);report.put("heightTransitions",transitions);
        report.put("seabedTopYRange",new int[]{-61,-59});
        report.put("lowestPossibleDepositBlockY",-49);
    }
}
