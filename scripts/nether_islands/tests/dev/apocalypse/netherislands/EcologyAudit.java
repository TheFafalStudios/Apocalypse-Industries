package dev.apocalypse.netherislands;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;

final class EcologyAudit {
    static void run(ServerLevel level,Map<String,Object> report) {
        var registry=level.registryAccess().registryOrThrow(Registries.BIOME);
        var source=level.getChunkSource().getGenerator().getBiomeSource();
        var climate=level.getChunkSource().randomState().sampler();
        var samples=new TreeMap<String,Integer>();
        var locations=new TreeMap<String,int[]>();
        for(int z=-4096;z<=4096;z+=64) for(int x=-4096;x<=4096;x+=64) {
            var biome=source.getNoiseBiome(x>>2,128>>2,z>>2,climate);
            String id=registry.getKey(biome.value()).toString();
            samples.merge(id,1,Integer::sum);
            if(id.startsWith("biomesoplenty:")) locations.putIfAbsent(id,new int[]{x,128,z});
        }
        report.put("biomeSourceSampleCountsAt128",samples);
        report.put("moddedBiomeLocations",locations);
        if(locations.size()!=5) throw new AssertionError("Not all five BOP Nether biomes sampled: "+locations.keySet());
        // Generate an island-rich region plus real chunks from each modded biome.
        var chunks=new LinkedHashSet<Long>();
        for(int z=-8;z<0;z++) for(int x=12;x<20;x++) chunks.add(net.minecraft.world.level.ChunkPos.asLong(x,z));
        for(var p:locations.values()) chunks.add(net.minecraft.world.level.ChunkPos.asLong(p[0]>>4,p[2]>>4));
        var generated=new ArrayList<net.minecraft.world.level.chunk.ChunkAccess>();
        for(long key:chunks) {var p=new net.minecraft.world.level.ChunkPos(key);generated.add(level.getChunk(p.x,p.z,ChunkStatus.FEATURES));}
        int glowstone=0;var columns=new HashSet<Long>();int min=999,max=-999;
        for(var chunk:generated) for(int z=0;z<16;z++) for(int x=0;x<16;x++) for(int y=101;y<312;y++) {
            int wx=chunk.getPos().getMinBlockX()+x,wz=chunk.getPos().getMinBlockZ()+z;
            if(chunk.getBlockState(new BlockPos(wx,y,wz)).is(Blocks.GLOWSTONE)) {
                glowstone++;columns.add(BlockPos.asLong(wx,0,wz));min=Math.min(min,y);max=Math.max(max,y);
            }
        }
        report.put("generatedChunksScanned",generated.size());report.put("glowstoneBlocks",glowstone);
        report.put("glowstoneColumns",columns.size());report.put("glowstoneYRange",new int[]{min,max});
        if(glowstone==0 || columns.size()<4) throw new AssertionError("No usable glowstone generated");
    }
}
