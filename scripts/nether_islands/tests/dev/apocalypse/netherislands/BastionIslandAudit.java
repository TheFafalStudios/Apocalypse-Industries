package dev.apocalypse.netherislands;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.*;

final class BastionIslandAudit {
    static void check(ServerLevel level,StructureStart start,Map<String,Object> report) {
        var box=BoundingBox.encapsulatingBoxes(start.getPieces().stream().map(p->p.getBoundingBox()).toList()).orElseThrow();
        if(box.minY()<112) throw new AssertionError("No visible bastion island clearance");
        int top=box.minY()-1,checked=0,solid=0,deep=0,rim=0,rimSolid=0;
        for(int z=box.minZ()-8;z<=box.maxZ()+8;z+=4) for(int x=box.minX()-8;x<=box.maxX()+8;x+=4) {
            var chunk=level.getChunk(x>>4,z>>4,ChunkStatus.FEATURES);
            boolean inner=x>=box.minX() && x<=box.maxX() && z>=box.minZ() && z<=box.maxZ();
            if(inner) {
                checked++;
                if(chunk.getBlockState(new BlockPos(x,top,z)).isSolid())solid++;
                if(chunk.getBlockState(new BlockPos(x,top-12,z)).isSolid())deep++;
            } else {
                rim++;
                if(chunk.getBlockState(new BlockPos(x,top-4,z)).isSolid())rimSolid++;
            }
        }
        report.put("completeStructureBounds",new int[]{box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ()});
        report.put("islandTopY",top);
        report.put("postStructureCoreColumnsChecked",checked);report.put("postStructureCoreColumnsSolid",solid);
        report.put("postStructureDeepColumnsSolid",deep);
        report.put("postStructureRimColumnsChecked",rim);report.put("postStructureRimColumnsSolid",rimSolid);
        if(solid!=checked || deep!=checked || rimSolid!=rim) throw new AssertionError("Incomplete visible bastion island after template placement");
    }
}
