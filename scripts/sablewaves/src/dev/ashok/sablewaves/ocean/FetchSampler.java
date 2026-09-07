package dev.ashok.sablewaves.ocean;
import dev.ashok.sablewaves.SeaTags;
import dev.ashok.sablewaves.WavesConfig;
import dev.ashok.sablewaves.hotfix.ChunkSafety;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
public final class FetchSampler {
    private FetchSampler() {}
    public static double sample(Level level, double x, double y, double z,
                                double windX, double windZ, WavesConfig config) {
        if (!config.fetchEnabled || level == null) return 1.0;
        double speed = Math.hypot(windX, windZ);
        if (speed < 1.0e-4) return 1.0;
        double max = Math.max(32.0, config.maxFetchBlocks), distance = max;
        int blockY = (int)Math.floor(y);
        for (int step = 1; step <= (int)(max / 6.0); step++) {
            int blockX = (int)Math.floor(x - windX / speed * 6.0 * step);
            int blockZ = (int)Math.floor(z - windZ / speed * 6.0 * step);
            // Unknown terrain is not evidence of a shoreline. Stop this ray.
            if (!ChunkSafety.loaded(level, blockX, blockZ)) break;
            if (!SeaTags.isSeaWater(ChunkSafety.fluid(level, new BlockPos(blockX, blockY, blockZ)))) {
                distance = step * 6.0; break;
            }
        }
        double minimum = Math.max(0.0, Math.min(1.0, config.minFetchFactor));
        return minimum + (1.0 - minimum) * Math.sqrt(Math.min(1.0, distance / max));
    }
}
