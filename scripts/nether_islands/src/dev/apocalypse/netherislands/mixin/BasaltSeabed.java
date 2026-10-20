package dev.apocalypse.netherislands.mixin;

import dev.apocalypse.netherislands.IslandFoundations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** TerraBlender can prepend its own bedrock rules; finalize this thin floor last. */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class BasaltSeabed {
    @Inject(method="buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V", at=@At("RETURN"))
    private void apocalypse$finishSeabed(WorldGenRegion region, StructureManager structures,
            RandomState random, ChunkAccess chunk, CallbackInfo ci) {
        if(!IslandFoundations.enabled(region) || !region.registryAccess().registryOrThrow(Registries.DENSITY_FUNCTION)
            .containsKey(ResourceLocation.parse("apocalypse_islands:seabed"))) return;
        var pos=new BlockPos.MutableBlockPos();
        int minX=chunk.getPos().getMinBlockX(),minZ=chunk.getPos().getMinBlockZ();
        for(int z=0;z<16;z++) for(int x=0;x<16;x++) {
            chunk.setBlockState(pos.set(minX+x,-64,minZ+z),Blocks.BEDROCK.defaultBlockState(),false);
            for(int y=-63;y<=-59;y++) {
                pos.set(minX+x,y,minZ+z);
                if(chunk.getBlockState(pos).isSolid())
                    chunk.setBlockState(pos,Blocks.BASALT.defaultBlockState(),false);
            }
        }
    }
}
