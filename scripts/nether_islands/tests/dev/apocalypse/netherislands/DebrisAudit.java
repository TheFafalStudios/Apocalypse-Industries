package dev.apocalypse.netherislands;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

final class DebrisAudit {
    static void run(ServerLevel level, Map<String,Object> report) {
        int biomes=0;
        var tag=TagKey.create(Registries.BIOME,ResourceLocation.parse("minecraft:is_nether"));
        var placed=level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        var custom=placed.get(ResourceLocation.parse("apocalypse_islands:floating_debris"));
        var large=placed.get(ResourceLocation.parse("minecraft:ore_ancient_debris_large"));
        var small=placed.get(ResourceLocation.parse("minecraft:ore_debris_small"));
        for(var holder:level.registryAccess().registryOrThrow(Registries.BIOME).holders().toList()) {
            if(!holder.is(tag)) continue;
            var features=holder.value().getGenerationSettings().features().stream()
                .flatMap(set->set.stream()).map(h->h.value()).toList();
            if(!features.contains(custom) || features.contains(large) || features.contains(small))
                throw new AssertionError("Wrong debris biome features: " + holder.key().location());
            biomes++;
        }
        report.put("netherBiomesWithReplacement",biomes);
        var chunks=new HashMap<Long,ChunkAccess>();
        // Generate the entire sample first: neighboring feature passes can write
        // into a chunk already visited. FEATURES includes carvers and surfaces.
        for(int z=-8;z<8;z++) for(int x=-8;x<8;x++)
            chunks.put(ChunkPos.asLong(x,z),level.getChunk(x,z,ChunkStatus.FEATURES));
        var debris=new HashSet<BlockPos>();
        int floor=0;
        for(var chunk:chunks.values()) {
            var c=chunk.getPos();
            for(int z=0;z<16;z++) for(int x=0;x<16;x++) {
                int wx=c.getMinBlockX()+x,wz=c.getMinBlockZ()+z;
                if(!chunk.getBlockState(new BlockPos(wx,-64,wz)).is(Blocks.BEDROCK))
                    throw new AssertionError("Bedrock regression");
                floor++;
                // Scan the entire dimension to detect leftover buried debris.
                for(int y=-63;y<level.getMaxBuildHeight();y++) {
                    var p=new BlockPos(wx,y,wz);
                    if(chunk.getBlockState(p).is(Blocks.ANCIENT_DEBRIS)) debris.add(p);
                }
            }
        }
        int minY=Integer.MAX_VALUE,maxY=Integer.MIN_VALUE;
        var casing=new HashSet<BlockPos>();
        for(var p:debris) {
            minY=Math.min(minY,p.getY()); maxY=Math.max(maxY,p.getY());
            for(var d:Direction.values()) {
                var n=p.relative(d);
                var chunk=chunks.get(ChunkPos.asLong(n.getX()>>4,n.getZ()>>4));
                if(chunk==null) continue;
                var state=chunk.getBlockState(n);
                if(!state.is(Blocks.ANCIENT_DEBRIS)) {
                    if(!state.is(Blocks.BLACKSTONE) && !state.is(Blocks.BASALT) && !state.is(Blocks.NETHERRACK))
                        throw new AssertionError("Missing rocky casing: " + p + " -> " + state);
                    casing.add(n);
                }
            }
        }
        var unseen=new HashSet<>(debris);
        var sizes=new ArrayList<Integer>();
        var depthSamples=new ArrayList<Object>();
        int deepCount=0,shallowCount=0,deepSum=0,shallowSum=0;
        int clipped=0;
        while(!unseen.isEmpty()) {
            var start=unseen.iterator().next();
            var queue=new ArrayDeque<BlockPos>(); queue.add(start); unseen.remove(start);
            int count=0,ySum=0; boolean edge=false;
            while(!queue.isEmpty()) {
                var p=queue.removeFirst(); count++;
                ySum+=p.getY();
                if(p.getX()==-128 || p.getX()==127 || p.getZ()==-128 || p.getZ()==127) edge=true;
                for(var d:Direction.values()) { var n=p.relative(d); if(unseen.remove(n)) queue.add(n); }
            }
            if(edge) clipped++;
            else {
                if(count<3 || count>24) throw new AssertionError("Wrong clump size: " + count);
                sizes.add(count);
                double meanY=ySum/(double)count;
                depthSamples.add(Map.of("meanY",meanY,"debrisBlocks",count));
                if(meanY<0) {deepCount++;deepSum+=count;}
                if(meanY>25) {shallowCount++;shallowSum+=count;}
            }
        }
        var palette=new TreeMap<String,Integer>();
        int lavaFaces=0;
        for(var p:casing) {
            var own=chunks.get(ChunkPos.asLong(p.getX()>>4,p.getZ()>>4));
            palette.merge(own.getBlockState(p).getBlock().getDescriptionId(),1,Integer::sum);
            for(var d:Direction.values()) {
                var n=p.relative(d);
                var chunk=chunks.get(ChunkPos.asLong(n.getX()>>4,n.getZ()>>4));
                if(chunk==null) continue;
                var state=chunk.getBlockState(n);
                if(state.is(Blocks.ANCIENT_DEBRIS) || casing.contains(n)) continue;
                // A casing neighbor at the sample boundary might belong to an
                // ore block outside the sample; don't classify that as terrain.
                if(n.getX()<=-127 || n.getX()>=126 || n.getZ()<=-127 || n.getZ()>=126) continue;
                if(!state.is(Blocks.LAVA)) throw new AssertionError("Casing touches terrain/air: " + p + " -> " + state);
                lavaFaces++;
            }
        }
        int maxCasingY=casing.stream().mapToInt(BlockPos::getY).max().orElseThrow();
        int lavaOverhead=level.getChunkSource().getGenerator().getSeaLevel()-1-maxCasingY;
        if(sizes.size()<4 || minY < -48 || maxY>68 || palette.size()!=3 || lavaOverhead<30
            || deepCount<2 || shallowCount<2 || deepSum/(double)deepCount<=shallowSum/(double)shallowCount+4)
            throw new AssertionError("Missing/out-of-range debris/casing palette");
        report.put("chunksScanned",chunks.size());
        report.put("debrisBlocks",debris.size());
        report.put("completeClumpSizes",sizes);
        report.put("clumpsByDepth",depthSamples);
        report.put("deepClumpCount",deepCount);report.put("shallowClumpCount",shallowCount);
        report.put("meanDeepClumpSize",deepSum/(double)deepCount);
        report.put("meanShallowClumpSize",shallowSum/(double)shallowCount);
        report.put("clumpsClippedBySampleBoundary",clipped);
        report.put("minimumDebrisY",minY); report.put("maximumDebrisY",maxY);
        report.put("casingBlocksChecked",casing.size());
        report.put("casingPalette",palette);
        report.put("outerLavaFacesChecked",lavaFaces);
        report.put("previousExpectedDebrisPerChunk",4.5/4);
        report.put("newExpectedDebrisPerChunk",13.5/12);
        report.put("highestCasingY",maxCasingY);
        report.put("minimumLavaBlocksAboveDeposit",lavaOverhead);
        report.put("bedrockFloorBlocksChecked",floor);
    }
}
