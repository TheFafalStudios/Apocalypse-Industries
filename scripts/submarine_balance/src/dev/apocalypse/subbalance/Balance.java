package dev.apocalypse.subbalance;

import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig;
import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig.HullProperty;
import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker;
import com.maxenonyme.createsubmarine.submarine.util.SubLevelRegistry;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import java.util.*;

public final class Balance {
    public static final TagKey<Block> VIEWPORTS = TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath("apocalypse_submarine_balance", "viewports"));
    public static String id(BlockState state) { return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(); }
    public static boolean window(BlockState state) {
        return Policy.namedWindow(id(state)) || state.is(VIEWPORTS)
            || state.getBlock() instanceof HalfTransparentBlock
            || state.getBlock() instanceof StainedGlassPaneBlock;
    }
    public static HullProperty property(BlockState state, HullProperty old, boolean lava, boolean copied) {
        return new HullProperty(Policy.cap(id(state),old.maxWaterDepth(),lava,window(state),copied), old.implosionChance());
    }
    public static Optional<HullProperty> property(BlockState state, UUID uuid) {
        return HullStrengthConfig.getFor(state).map(p -> property(state,p,lava(uuid),false));
    }
    /** Same rounded world-space origin and real-fluid lookup as upstream measureColumn.
     * Never requests a new chunk. No retained material/fluid cache can cross a fluid transition. */
    public static boolean lava(UUID uuid) {
        var access = SubLevelRegistry.getAll().get(uuid);
        var world = access instanceof SubLevel sub ? sub.getLevel() : SubLevelRegistry.getLevel(uuid);
        if (access == null || world == null) return false;
        var p = access.logicalPose().position();
        var pos = new BlockPos((int)Math.round(p.x()), (int)Math.round(p.y()), (int)Math.round(p.z()));
        var chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        return chunk != null && CompartmentTracker.realFluidState(chunk,pos).is(FluidTags.LAVA);
    }
}
