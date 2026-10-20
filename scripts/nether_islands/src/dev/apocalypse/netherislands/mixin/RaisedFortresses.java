package dev.apocalypse.netherislands.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherFortressStructure.class)
public abstract class RaisedFortresses {
    @Inject(method="generatePieces",at=@At("RETURN"))
    private static void apocalypse$followSea(StructurePiecesBuilder builder,Structure.GenerationContext context,CallbackInfo ci) {
        if(context.chunkGenerator().getSeaLevel()==100 && context.registryAccess().registryOrThrow(Registries.DENSITY_FUNCTION)
            .containsKey(ResourceLocation.parse("apocalypse_islands:foundations_enabled")))
            builder.offsetPiecesVertically(68);
    }
}
