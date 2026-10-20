package dev.apocalypse.subbalance.mixin;
import dev.apocalypse.subbalance.*;
import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig;
import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig.HullProperty;
import com.maxenonyme.createsubmarine.submarine.system.SubmarinePressureSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=SubmarinePressureSystem.class,remap=false)
public abstract class ActualMaterials {
    @Inject(method="getActualBlockState",at=@At("RETURN"),cancellable=true)
    private static void ai$material(Level level,BlockPos pos,BlockState frame,CallbackInfoReturnable<BlockState> cir) {
        var be=level.getBlockEntity(pos);
        if (be!=null && BalanceMod.eligible(frame) && be.getPersistentData().getBoolean(BalanceMod.REINFORCED)) {
            // Only the pressure-material lookup changes; fluid handling and block behavior do not.
            cir.setReturnValue(Blocks.NETHERITE_BLOCK.defaultBlockState());
        } else if (cir.getReturnValue().is(Blocks.NETHERITE_BLOCK) && !frame.is(Blocks.NETHERITE_BLOCK)) {
            // Copycat coverings do not contain a complete nine-ingot load-bearing block.
            cir.setReturnValue(BuiltInRegistries.BLOCK.get(ResourceLocation.parse("dndecor:netherite_sheet_metal")).defaultBlockState());
        }
    }
    @Redirect(method="copiedMaterial",at=@At(value="INVOKE",target="Lcom/maxenonyme/createsubmarine/submarine/config/HullStrengthConfig;getFor(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"))
    private static Optional<HullProperty> ai$weakestCopied(BlockState state) {
        // Upstream selects the minimum. Use lava ordering so equal water tiers cannot hide weaker steel.
        return HullStrengthConfig.getFor(state).map(p -> Balance.property(state,p,true,true));
    }
}
