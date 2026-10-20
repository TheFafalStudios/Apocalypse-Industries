package dev.apocalypse.netherislands.mixin;

import java.util.function.Predicate;
import java.util.List;
import dev.apocalypse.netherislands.IslandFoundations;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Structure.class)
public abstract class BastionClearance {
    @Inject(method="generate",at=@At("RETURN"),cancellable=true)
    private void apocalypse$visibleBase(RegistryAccess registry,ChunkGenerator generator,BiomeSource biomes,
            RandomState random,StructureTemplateManager templates,long seed,ChunkPos chunk,int references,
            LevelHeightAccessor height,Predicate<Holder<Biome>> valid,CallbackInfoReturnable<StructureStart> cir) {
        // All real Nether structures, including modded jigsaws, need clearance.
        // Vanilla can supply a ChunkAccess rather than a LevelAccessor here.
        // Scope that path by the custom sea/marker AND actual Nether biome source.
        if(generator.getSeaLevel()!=100 || !registry.registryOrThrow(Registries.DENSITY_FUNCTION)
            .containsKey(ResourceLocation.parse("apocalypse_islands:foundations_enabled"))
            || generator.getBiomeSource().possibleBiomes().stream().noneMatch(b->b.is(BiomeTags.IS_NETHER)))return;
        if(height instanceof LevelAccessor access && !IslandFoundations.enabled(access))return;
        var start=cir.getReturnValue();
        if(start==null || !start.isValid()) return;
        int min=start.getPieces().stream().mapToInt(p->p.getBoundingBox().minY()).min().orElseThrow();
        int max=start.getPieces().stream().mapToInt(p->p.getBoundingBox().maxY()).max().orElseThrow();
        int lower=generator.getSeaLevel()+12-min,upper=height.getMaxBuildHeight()-1-max;
        if(lower>upper) {
            org.slf4j.LoggerFactory.getLogger("apocalypse_nether_islands").warn(
                "Structure cannot fit above custom lava sea: {}",registry.registryOrThrow(Registries.STRUCTURE).getKey(start.getStructure()));
            return;
        }
        int shift=Math.max(lower,Math.min(0,upper));
        if(shift==0) return;
        for(var piece:start.getPieces()) {
            piece.move(0,shift,0);
            // PoolElementStructurePiece.move translates its box/template origin,
            // but vanilla leaves the absolute terrain-junction Y coordinates unchanged.
            if(piece instanceof PoolElementStructurePiece pool) {
                var old=List.copyOf(pool.getJunctions());pool.getJunctions().clear();
                for(var j:old)pool.addJunction(new JigsawJunction(j.getSourceX(),
                    j.getSourceGroundY()+shift,j.getSourceZ(),j.getDeltaY(),j.getDestProjection()));
            }
        }
        // A fresh start avoids retaining a cached bounding box at the old height.
        cir.setReturnValue(new StructureStart(start.getStructure(),chunk,references,new PiecesContainer(start.getPieces())));
    }
}
