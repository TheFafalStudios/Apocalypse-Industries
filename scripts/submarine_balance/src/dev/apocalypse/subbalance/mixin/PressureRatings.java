package dev.apocalypse.subbalance.mixin;
import dev.apocalypse.subbalance.Balance;
import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig.HullProperty;
import com.maxenonyme.createsubmarine.submarine.system.SubmarinePressureSystem;
import com.maxenonyme.createsubmarine.submarine.util.SubLevelRegistry;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=SubmarinePressureSystem.class,remap=false)
public abstract class PressureRatings {
    @Shadow @Final private static Map<UUID,?> MATERIALS;
    @Shadow @Final private static Map<UUID,?> WEAK_CACHE;
    // A fresh scan per pressure cycle also sees material edits and girder loss immediately.
    @Inject(method="processSubmarine",at=@At("HEAD"))
    private static void ai$fresh(UUID id,SubLevelAccess access,CallbackInfo ci) {
        MATERIALS.remove(id); WEAK_CACHE.remove(id);
    }
    @Inject(method="invalidate",at=@At("HEAD"))
    private static void ai$edit(UUID id,CallbackInfo ci) { WEAK_CACHE.remove(id); }
    @Redirect(method="processSubmarine",at=@At(value="INVOKE",target="Lcom/maxenonyme/createsubmarine/submarine/config/HullStrengthConfig;getFor(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"))
    private static Optional<HullProperty> ai$live(BlockState state,UUID id,SubLevelAccess access) { return Balance.property(state,id); }
    @Redirect(method="materials",at=@At(value="INVOKE",target="Lcom/maxenonyme/createsubmarine/submarine/config/HullStrengthConfig;getFor(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"))
    private static Optional<HullProperty> ai$materials(BlockState state,UUID id,Level level,Long2FloatOpenHashMap shape) { return Balance.property(state,id); }
    @Redirect(method="scanWeakest",at=@At(value="INVOKE",target="Lcom/maxenonyme/createsubmarine/submarine/config/HullStrengthConfig;getFor(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"))
    private static Optional<HullProperty> ai$diagnostics(BlockState state,UUID id,Level level) { return Balance.property(state,id); }
    @ModifyVariable(method="effectiveDepth",at=@At("HEAD"),argsOnly=true,ordinal=0)
    private static int ai$effective(int rating,UUID id,BlockPos pos,int original) {
        Level level = SubLevelRegistry.getLevel(id);
        if (level == null) return rating;
        BlockState state = SubmarinePressureSystem.getActualBlockState(level,pos,level.getBlockState(pos));
        return Balance.property(state,new HullProperty(rating,0),Balance.lava(id),false).maxWaterDepth();
    }
}
