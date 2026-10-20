package dev.apocalypse.netherislands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Connected ore bodies with a rocky casing, placed atomically in open lava. */
public final class FloatingDebris extends Feature<FloatingDebris.Config> {
    public static final DeferredRegister<Feature<?>> TYPES =
        DeferredRegister.create(Registries.FEATURE, "apocalypse_islands");
    static { TYPES.register("floating_debris", FloatingDebris::new); }

    public record Gradient(int minY,int maxY,int shallowMin,int shallowMax) {
        static final Codec<Gradient> CODEC=RecordCodecBuilder.<Gradient>create(i->i.group(
            Codec.INT.fieldOf("min_y").forGetter(Gradient::minY),
            Codec.INT.fieldOf("max_y").forGetter(Gradient::maxY),
            Codec.intRange(1,64).fieldOf("shallow_min").forGetter(Gradient::shallowMin),
            Codec.intRange(1,64).fieldOf("shallow_max").forGetter(Gradient::shallowMax)
        ).apply(i,Gradient::new)).validate(g->g.minY<g.maxY && g.shallowMin<=g.shallowMax
            ?DataResult.success(g):DataResult.error(()->"Invalid depth gradient"));
    }

    public record Config(int minBlocks, int maxBlocks, BlockStateProvider casing,Optional<Gradient> gradient) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.<Config>create(i -> i.group(
            Codec.intRange(1, 64).fieldOf("min_blocks").forGetter(Config::minBlocks),
            Codec.intRange(1, 64).fieldOf("max_blocks").forGetter(Config::maxBlocks),
            BlockStateProvider.CODEC.fieldOf("casing").forGetter(Config::casing),
            Gradient.CODEC.optionalFieldOf("depth_gradient").forGetter(Config::gradient)
        ).apply(i, Config::new)).validate(c -> c.minBlocks <= c.maxBlocks
            ? DataResult.success(c) : DataResult.error(() -> "min_blocks exceeds max_blocks"));
    }

    public FloatingDebris() { super(Config.CODEC); }

    private static boolean openLava(WorldGenLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(Blocks.LAVA)) return false;
        for (Direction side : Direction.values())
            if (!level.getBlockState(pos.relative(side)).is(Blocks.LAVA)) return false;
        return true;
    }

    @Override public boolean place(FeaturePlaceContext<Config> context) {
        var level = context.level();
        if (!IslandFoundations.enabled(level)) return false;
        var random = context.random();
        var config = context.config();
        var origin = context.origin().immutable();
        int min=config.minBlocks,max=config.maxBlocks;
        if(config.gradient.isPresent()) {
            var g=config.gradient.get();
            double depth=Math.max(0,Math.min(1,(g.maxY-origin.getY())/(double)(g.maxY-g.minY)));
            // Round the depth increment symmetrically: paired shallow/deep
            // heights keep the original expected ore budget instead of rounding
            // every half-step upward.
            min=g.shallowMin+(int)Math.rint((min-g.shallowMin)*depth);
            max=g.shallowMax+(int)Math.rint((max-g.shallowMax)*depth);
        }
        int size=min+random.nextInt(max-min+1);
        if (!openLava(level, origin)) return false;
        var clump = new ArrayList<BlockPos>();
        clump.add(origin);
        // Grow face-connected blocks, keeping the entire outside shell in lava.
        // Validate the whole clump before any writes, so failures leave no scraps.
        for (int attempt = 0; clump.size() < size && attempt < size * 32; attempt++) {
            var candidate = clump.get(random.nextInt(clump.size()))
                .relative(Direction.values()[random.nextInt(6)]);
            if (candidate.distSqr(origin) <= 16 && !clump.contains(candidate)
                && openLava(level, candidate)) clump.add(candidate);
        }
        if (clump.size() != size) return false;
        var shell = new HashSet<BlockPos>();
        for (var pos : clump) for (var side : Direction.values()) {
            var neighbor = pos.relative(side);
            if (!clump.contains(neighbor)) shell.add(neighbor);
        }
        // Keep the rock casing itself submerged and detached from terrain.
        // Checking before any writes also prevents partial or clipped deposits.
        for (var pos : shell) if (!openLava(level, pos)) return false;
        // Stable ordering keeps palette selection independent of hash iteration.
        for (var pos : shell.stream().sorted(java.util.Comparator.comparingLong(BlockPos::asLong)).toList())
            level.setBlock(pos, config.casing.getState(random, pos), 2);
        for (var pos : clump) level.setBlock(pos, Blocks.ANCIENT_DEBRIS.defaultBlockState(), 2);
        return true;
    }
}
