package dev.apocalypse.netherislands.mixin;

import dev.apocalypse.netherislands.IslandFoundations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PortalForcer.class)
public abstract class SafeNetherPortals {
    @Shadow @Final private ServerLevel level;

    private int apocalypse$minimumY() {
        return IslandFoundations.seaLevel(level)+4;
    }

    @Inject(method="canHostFrame",at=@At("HEAD"),cancellable=true)
    private void apocalypse$aboveLava(BlockPos base,BlockPos.MutableBlockPos cursor,
            Direction direction,int offset,CallbackInfoReturnable<Boolean> cir) {
        if(IslandFoundations.enabled(level) && base.getY()<apocalypse$minimumY()) cir.setReturnValue(false);
    }

    @ModifyConstant(method="createPortal",constant=@Constant(intValue=70))
    private int apocalypse$raiseFallback(int original) {
        return IslandFoundations.enabled(level)?Math.max(original,apocalypse$minimumY()):original;
    }

    @Inject(method="lambda$findClosestPortalPosition$1",at=@At("HEAD"),cancellable=true)
    private void apocalypse$skipUnsafeExisting(BlockPos pos,CallbackInfoReturnable<Boolean> cir) {
        if(!IslandFoundations.enabled(level)) return;
        var state=level.getBlockState(pos);
        if(!state.is(Blocks.NETHER_PORTAL)) {cir.setReturnValue(false);return;}
        BlockPos bottom=pos;
        while(bottom.getY()>level.getMinBuildHeight() && level.getBlockState(bottom.below()).is(Blocks.NETHER_PORTAL))
            bottom=bottom.below();
        if(bottom.getY()<apocalypse$minimumY()) {cir.setReturnValue(false);return;}
        var along=state.getValue(NetherPortalBlock.AXIS)==Direction.Axis.X?Direction.EAST:Direction.SOUTH;
        var normal=along.getClockWise();
        while(level.getBlockState(bottom.relative(along.getOpposite())).is(Blocks.NETHER_PORTAL))
            bottom=bottom.relative(along.getOpposite());
        for(var column=bottom;level.getBlockState(column).is(Blocks.NETHER_PORTAL);column=column.relative(along)) {
            for(var p=column;level.getBlockState(p).is(Blocks.NETHER_PORTAL);p=p.above()) {
                if(level.getFluidState(p.relative(normal)).is(FluidTags.LAVA)
                    || level.getFluidState(p.relative(normal.getOpposite())).is(FluidTags.LAVA)) {
                    cir.setReturnValue(false);return;
                }
            }
        }
    }
}
