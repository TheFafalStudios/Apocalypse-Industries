package dev.apocalypse.netherislands;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.*;

/** All structures in the actual Nether generator's enabled structure sets. */
final class NetherStructureAudit {
    static void run(ServerLevel level,Map<String,Object> report) {
        var generator=level.getChunkSource().getGenerator();
        var registry=level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var selected=new LinkedHashSet<Structure>();
        for(var set:level.getChunkSource().getGeneratorState().possibleStructureSets())
            for(var entry:set.value().structures()) selected.add(entry.structure().value());
        report.put("eligibleStructures",selected.stream().map(s->registry.getKey(s).toString()).sorted().toList());
        System.out.println("NETHER STRUCTURE INVENTORY "+report.get("eligibleStructures"));
        var rows=new ArrayList<Object>();report.put("structureSamples",rows);
        var failures=new ArrayList<String>();report.put("structureFailures",failures);
        var layouts=new HashSet<String>();
        for(var structure:selected) {
            String id=registry.getKey(structure).toString();var seen=new HashSet<Long>();
            int wanted=id.equals("minecraft:bastion_remnant")?12:1;
            for(int i=0;i<24 && seen.size()<wanted;i++) {
                int ox=(i%5-2)*2048,oz=(i/5-2)*2048;
                var found=generator.findNearestMapStructure(level,HolderSet.direct(registry.wrapAsHolder(structure)),new BlockPos(ox,100,oz),64,false);
                if(found==null)continue;
                var p=found.getFirst();long key=net.minecraft.world.level.ChunkPos.asLong(p.getX()>>4,p.getZ()>>4);
                if(!seen.add(key))continue;
                var origin=level.getChunk(p.getX()>>4,p.getZ()>>4,ChunkStatus.STRUCTURE_STARTS);
                var start=origin.getStartForStructure(structure);
                if(start==null||!start.isValid()){failures.add(id+" missing start");continue;}
                var b=BoundingBox.encapsulatingBoxes(start.getPieces().stream().map(StructurePiece::getBoundingBox).toList()).orElseThrow();
                String layout=start.getPieces().getFirst() instanceof PoolElementStructurePiece piece?piece.getElement().toString():start.getPieces().getFirst().getClass().getSimpleName();
                if(id.equals("minecraft:bastion_remnant"))layouts.add(layout);
                var row=new LinkedHashMap<String,Object>();rows.add(row);row.put("id",id);row.put("layout",layout);
                row.put("bounds",new int[]{b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()});row.put("pieces",start.getPieces().size());
                if(b.minY()<generator.getSeaLevel()+12)failures.add(id+" below clearance at "+p+" minY="+b.minY());
                if(b.maxY()>=level.getMaxBuildHeight())failures.add(id+" clipped above build height");
                int tested=0,solid=0;var missing=new ArrayList<Object>();
                // Full structures and neighboring features must have been placed, not just noise.
                for(int z:new int[]{b.minZ(),(b.minZ()+b.maxZ())/2,b.maxZ()})for(int x:new int[]{b.minX(),(b.minX()+b.maxX())/2,b.maxX()}){
                    var chunk=level.getChunk(x>>4,z>>4,ChunkStatus.FEATURES);tested++;
                    var state=chunk.getBlockState(new BlockPos(x,b.minY()-1,z));
                    if(state.isSolid())solid++;
                    else missing.add(List.of(x,b.minY()-1,z,state.toString()));
                }
                row.put("basePoints",tested);row.put("solidBasePoints",solid);row.put("nonSolidBaseSamples",missing);
                // A placed block may extend one block below the template bounds.
                // Verify a continuous mineral base below that placement, too.
                if(!id.equals("minecraft:fortress")) {
                    int total=0,base=0,deep=0;
                    for(int z=b.minZ();z<=b.maxZ();z+=4)for(int x=b.minX();x<=b.maxX();x+=4){
                        var chunk=level.getChunk(x>>4,z>>4,ChunkStatus.FEATURES);total++;
                        if(chunk.getBlockState(new BlockPos(x,b.minY()-2,z)).isSolid())base++;
                        if(chunk.getBlockState(new BlockPos(x,b.minY()-12,z)).isSolid())deep++;
                    }
                    row.put("denseBasePoints",total);row.put("denseBaseSolid",base);row.put("denseDeepSolid",deep);
                    if(base!=total||deep!=total)failures.add(id+" incomplete dense island base="+base+" deep="+deep+" total="+total);
                }
                System.out.println("NETHER STRUCTURE AUDIT "+id+" "+layout+" minY="+b.minY()+" base="+solid+"/"+tested);
                try {java.nio.file.Files.writeString(java.nio.file.Path.of("structure-audit-progress.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));}
                catch(java.io.IOException e){throw new RuntimeException(e);}
                if(id.equals("minecraft:bastion_remnant") && layouts.size()>=4)break;
            }
            if(seen.isEmpty())failures.add("No natural sample for enabled structure "+id);
        }
        report.put("bastionLayouts",layouts.stream().sorted().toList());
        if(layouts.size()<4)failures.add("Did not cover all four bastion root layouts: "+layouts);
        if(!failures.isEmpty())throw new AssertionError(failures.toString());
    }
}
