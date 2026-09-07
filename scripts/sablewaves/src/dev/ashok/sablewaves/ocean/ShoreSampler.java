package dev.ashok.sablewaves.ocean;
import dev.ashok.sablewaves.SeaTags;
import dev.ashok.sablewaves.hotfix.ChunkSafety;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
/** World-local soundings. Missing chunks never become invented coastlines. */
public final class ShoreSampler {
    private static final Map<Level, Map<Key, Shore>> CACHES = new WeakHashMap<>();
    private static final long TTL_NANOS = 2_000_000_000L;
    private static final Shore OPEN_SEA = new Shore();
    private record Key(int x, int y, int z, int steps) {}
    private ShoreSampler() {}
    private static Map<Key, Shore> cache(Level level) {
        synchronized (CACHES) {
            return CACHES.computeIfAbsent(level, ignored -> new ConcurrentHashMap<>());
        }
    }
    public static Shore sample(Level level, double x, double y, double z, double radius) {
        if (level == null) return OPEN_SEA;
        int cellX = Math.floorDiv((int)Math.floor(x), 8), cellZ = Math.floorDiv((int)Math.floor(z), 8);
        int steps = Math.max(1, (int)(Math.max(8.0, radius) / 2.0));
        Key key = new Key(cellX, (int)Math.floor(y), cellZ, steps);
        Map<Key, Shore> cache = cache(level);
        long now = System.nanoTime();
        Shore existing = cache.get(key);
        if (existing != null && now - existing.stamp < TTL_NANOS) return existing;
        if (cache.size() > 4096) cache.clear();
        Shore shore = new Shore(); shore.stamp = now;
        double originX = cellX * 8.0 + 4.0, originZ = cellZ * 8.0 + 4.0;
        int waterY = waterYNear(level, originX, originZ, y);
        if (waterY == Integer.MIN_VALUE) {
            search: for (int dx = 0; dx < 8; dx += 2) {
                for (int dz = 0; dz < 8; dz += 2) {
                    double px = cellX * 8.0 + dx + 0.5, pz = cellZ * 8.0 + dz + 0.5;
                    int py = waterYNear(level, px, pz, y);
                    if (py != Integer.MIN_VALUE) {
                        originX = px; originZ = pz; waterY = py; break search;
                    }
                }
            }
        }
        if (waterY != Integer.MIN_VALUE) {
            for (int ray = 0; ray < 12; ray++) {
                double angle = ray * Math.PI / 6.0, dx = Math.cos(angle), dz = Math.sin(angle);
                for (int step = 1; step <= steps; step++) {
                    int state = water(level, originX + dx * 2.0 * step, waterY, originZ + dz * 2.0 * step);
                    if (state < 0) break;
                    if (state == 1) continue;
                    double distance = step * 2.0;
                    if (distance < shore.distance) {
                        shore.found = true; shore.distance = distance;
                        shore.offshoreX = -dx; shore.offshoreZ = -dz;
                    }
                    break;
                }
            }
            for (int depth = 0; depth < 72; depth++) {
                int state = water(level, originX, waterY - depth, originZ);
                if (state < 0) break;
                if (state == 0) { shore.waterDepth = depth; break; }
            }
        }
        cache.put(key, shore);
        return shore;
    }
    private static int waterYNear(Level level, double x, double z, double y) {
        int blockY = (int)Math.floor(y);
        for (int offset = 0; offset <= 6; offset++) {
            if (water(level, x, blockY - offset, z) == 1) return blockY - offset;
            if (offset > 0 && water(level, x, blockY + offset, z) == 1) return blockY + offset;
        }
        return Integer.MIN_VALUE;
    }
    private static int water(Level level, double x, int y, double z) {
        int bx = (int)Math.floor(x), bz = (int)Math.floor(z);
        if (!ChunkSafety.loaded(level, bx, bz)) return -1;
        return SeaTags.isSeaWater(ChunkSafety.fluid(level, new BlockPos(bx, y, bz))) ? 1 : 0;
    }
    public static void clear() { synchronized (CACHES) { CACHES.clear(); } }
    public static final class Shore {
        public double distance = Double.MAX_VALUE;
        public double offshoreX;
        public double offshoreZ = 1.0;
        public double waterDepth = 72.0;
        public boolean found;
        private long stamp;
    }
}
