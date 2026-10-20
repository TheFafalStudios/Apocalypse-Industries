package dev.apocalypse.netherislands.mixin;

import java.util.List;
import dev.apocalypse.netherislands.IslandFoundations;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Finish only new structure-generation chunks; never writes through template rooms. */
@Mixin(StructureStart.class)
public abstract class FinalStructureFoundations {
    @Inject(method="placeInChunk",at=@At("RETURN"))
    private void apocalypse$finishIsland(WorldGenLevel level,StructureManager manager,
            ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,CallbackInfo ci) {
        if(!IslandFoundations.enabled(level))return;
        var start=(StructureStart)(Object)this;
        if(!start.isValid() || start.getStructure() instanceof NetherFortressStructure)return;
        var b=BoundingBox.encapsulatingBoxes(start.getPieces().stream().map(StructurePiece::getBoundingBox).toList()).orElseThrow();
        var footprint=new IslandFoundations.Footprint(b.minX(),b.minZ(),b.maxX(),b.maxZ(),b.minY()-1,generator.getSeaLevel(),true);
        int low=Math.max(level.getMinBuildHeight(),b.minY()-42),high=Math.min(level.getMaxBuildHeight()-1,b.minY()-1);
        var pos=new BlockPos.MutableBlockPos();
        for(int z=chunk.getMinBlockZ();z<=chunk.getMaxBlockZ();z++)
            for(int x=chunk.getMinBlockX();x<=chunk.getMaxBlockX();x++) {
                if(!footprint.affects(x,z))continue;
                for(int y=low;y<=high;y++) {
                    if(!clip.isInside(pos.set(x,y,z)) || footprint.contribution(x,y,z)<=0)continue;
                    var state=level.getBlockState(pos);
                    // Keep structural blocks/containers/processors' placed content.
                    // Only repair empty space or sea below the complete template bounds.
                    if(state.isAir() || state.getBlock() instanceof LiquidBlock)
                        level.setBlock(pos,Blocks.NETHERRACK.defaultBlockState(),2);
                }
            }
    }
}
