package dev.apocalypse.netherislands.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.apocalypse.netherislands.IslandFoundations;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ChunkGenerator.class)
public abstract class StructureReferences {
    // Include the apron in neighboring chunks' normal structure references. Uses
    // vanilla's existing 8-chunk start scan; never requests an extra chunk status.
    @WrapOperation(method="createReferences", at=@At(value="INVOKE",
        target="Lnet/minecraft/world/level/levelgen/structure/BoundingBox;intersects(IIII)Z"))
    private boolean apocalypse$apronReferences(BoundingBox box, int x0, int z0, int x1, int z1,
            Operation<Boolean> original, WorldGenLevel level, StructureManager manager, ChunkAccess chunk) {
        int pad = IslandFoundations.enabled(level) ? IslandFoundations.BASTION_MARGIN : 0;
        return original.call(box, x0 - pad, z0 - pad, x1 + pad, z1 + pad);
    }
}
