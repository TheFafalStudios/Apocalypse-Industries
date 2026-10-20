package dev.apocalypse.netherislands.mixin;
import dev.apocalypse.netherislands.ColumnCache;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(NoiseChunk.class)
public abstract class ExactIslandColumns {
    @Shadow @Final int firstNoiseX;
    @Shadow @Final int firstNoiseZ;
    @Shadow @Final int noiseSizeXZ;
    @Inject(method="wrapNew", at=@At("HEAD"), cancellable=true)
    private void apocalypse$columns(DensityFunction function, CallbackInfoReturnable<DensityFunction> cir) {
        if(function instanceof ColumnCache cache)
            cir.setReturnValue(new ColumnCache.Resolved(cache,QuartPos.toBlock(firstNoiseX),
                QuartPos.toBlock(firstNoiseZ),QuartPos.toBlock(noiseSizeXZ)+1));
    }
}
