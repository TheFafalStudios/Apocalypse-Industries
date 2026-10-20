package dev.apocalypse.netherislands.mixin;
import java.util.List;
import dev.apocalypse.netherislands.IslandFoundations;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Beardifier.class)
public abstract class StructureFoundations {
    @Unique private List<IslandFoundations.Footprint> apocalypse$footprints = List.of();

    @Inject(method="forStructuresInChunk", at=@At("RETURN"))
    private static void apocalypse$capture(StructureManager manager, ChunkPos pos,
            CallbackInfoReturnable<Beardifier> cir) {
        if (!IslandFoundations.enabled(((StructureManagerAccess)manager).apocalypse$level())) return;
        var self = (StructureFoundations)(Object)cir.getReturnValue();
        self.apocalypse$footprints = IslandFoundations.footprints(
            manager.startsForStructure(pos, structure -> true),
            IslandFoundations.seaLevel(((StructureManagerAccess)manager).apocalypse$level()),
            pos.getMinBlockX(), pos.getMinBlockZ(),
            ((StructureManagerAccess)manager).apocalypse$level().registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .get(net.minecraft.resources.ResourceLocation.parse("minecraft:bastion_remnant")));
    }

    @Inject(method="compute", at=@At("RETURN"), cancellable=true)
    private void apocalypse$support(DensityFunction.FunctionContext context, CallbackInfoReturnable<Double> cir) {
        double support = 0;
        for (var footprint : apocalypse$footprints) {
            if (footprint.affects(context.blockX(), context.blockZ()))
                support = Math.max(support, footprint.contribution(context.blockX(), context.blockY(), context.blockZ()));
        }
        if (support > 0) cir.setReturnValue(cir.getReturnValue() + support);
    }
}
