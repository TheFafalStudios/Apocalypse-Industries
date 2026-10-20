package dev.apocalypse.netherislands;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.DensityFunction;

final class LayerAudit {
    static void run(ServerLevel level, Map<String,Object> report) {
        var registry=level.registryAccess().registryOrThrow(Registries.DENSITY_FUNCTION);
        var random=level.getChunkSource().randomState();
        var seedNoise=new DensityFunction.Visitor() {
            @Override public DensityFunction apply(DensityFunction f) { return f; }
            @Override public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder h) {
                return new DensityFunction.NoiseHolder(h.noiseData(),random.getOrCreateNoise(h.noiseData().unwrapKey().orElseThrow()));
            }
        };
        var functions=new DensityFunction[4][2];
        for(int layer=0;layer<4;layer++) for(int side=0;side<2;side++)
            functions[layer][side]=registry.get(ResourceLocation.parse("diabolical_islands:islands/layer"+(layer+1)
                +"/offset_"+(side==0?"bottom":"top"))).mapAll(seedNoise);
        int n=256,step=4,totalLow=0,totalHigh=0;
        double[] best={0,0,0}; int[][] representatives=new int[3][2];
        var regions=new ArrayList<Object>();
        for(int center:new int[]{-2048,0,2048}) {
            boolean[][] masks=new boolean[4][n*n];
            for(int z=0;z<n;z++) for(int x=0;x<n;x++) {
                var context=new DensityFunction.SinglePointContext(center+(x-n/2)*step,48,center+(z-n/2)*step);
                for(int layer=0;layer<4;layer++) {
                    double score=Math.min(functions[layer][0].compute(context),functions[layer][1].compute(context));
                    int band=Math.max(0,layer-1);
                    if(score>0) masks[band][z*n+x]=true;
                    if(layer<2 && score>Math.pow(28.0/64,2)) masks[3][z*n+x]=true;
                    if(score>best[band]) {
                        best[band]=score;
                        representatives[band]=new int[]{context.blockX(),context.blockZ()};
                    }
                }
            }
            int allLow=count(masks[0],n),low=count(masks[0],n,masks[3]),high1=count(masks[1],n),high2=count(masks[2],n);
            totalLow+=low; totalHigh+=high1+high2+allLow-low;
            regions.add(Map.of("center",center,"lavaContact",low,"smallLowAirborne",allLow-low,"middleAirborne",high1,"highAirborne",high2));
        }
        report.put("densityFootprintRegions",regions);
        report.put("footprintSampleStep",step);
        report.put("footprintRegionWidth",n*step);
        report.put("minimumCountedIslandArea",64);
        report.put("lavaContactIslands",totalLow); report.put("airborneIslands",totalHigh);
        double percent=totalLow+totalHigh==0?0:100.0*totalLow/(totalLow+totalHigh);
        report.put("lavaContactIslandPercent",percent);
        if(totalLow<10 || totalHigh<10) throw new AssertionError("Too few islands in sample");
        int[] solids=new int[3]; int floor=0,contact=0;
        var sampleChunks=new java.util.LinkedHashSet<Long>();
        for(int cz=-3;cz<3;cz++) for(int cx=-3;cx<3;cx++)
            sampleChunks.add(net.minecraft.world.level.ChunkPos.asLong(cx,cz));
        for(var p:representatives) sampleChunks.add(net.minecraft.world.level.ChunkPos.asLong(p[0]>>4,p[1]>>4));
        for(long key:sampleChunks) {
            var position=new net.minecraft.world.level.ChunkPos(key);
            int cx=position.x,cz=position.z;
            var chunk=level.getChunk(cx,cz,ChunkStatus.NOISE);
            for(int z=0;z<16;z++) for(int x=0;x<16;x++) {
                int wx=cx*16+x,wz=cz*16+z;
                if(!chunk.getBlockState(new BlockPos(wx,-64,wz)).isSolid()) throw new AssertionError("Missing floor density");
                floor++;
                if(chunk.getBlockState(new BlockPos(wx,100,wz)).isSolid()) contact++;
                for(int band=0;band<3;band++) {
                    int start=new int[]{101,184,256}[band],end=new int[]{184,240,312}[band];
                    for(int y=start;y<end;y++) if(chunk.getBlockState(new BlockPos(wx,y,wz)).isSolid()) solids[band]++;
                }
            }
        }
        report.put("representativeBandCoordinates",representatives);
        report.put("actualNoiseChunks",sampleChunks.size()); report.put("solidBlocksByHeightBand",solids);
        report.put("actualLavaContactColumns",contact); report.put("solidFloorColumnsChecked",floor);
        if(java.util.Arrays.stream(solids).anyMatch(v->v==0))
            throw new AssertionError("Island balance/bands failed");
    }

    private static int count(boolean[] mask,int n) {
        return count(mask,n,null);
    }
    private static int count(boolean[] mask,int n,boolean[] contact) {
        boolean[] seen=new boolean[mask.length]; int islands=0;
        int[] queue=new int[mask.length];
        for(int start=0;start<mask.length;start++) {
            if(!mask[start] || seen[start]) continue;
            int head=0,tail=0; queue[tail++]=start;seen[start]=true;boolean edge=false,touches=false;
            while(head<tail) {
                int i=queue[head++],x=i%n,z=i/n;
                if(contact==null || contact[i]) touches=true;
                if(x==0 || z==0 || x==n-1 || z==n-1) edge=true;
                for(int next:new int[]{x>0?i-1:-1,x<n-1?i+1:-1,z>0?i-n:-1,z<n-1?i+n:-1})
                    if(next>=0 && mask[next] && !seen[next]) {seen[next]=true;queue[tail++]=next;}
            }
            if(!edge && tail>=4 && touches) islands++;
        }
        return islands;
    }
}
