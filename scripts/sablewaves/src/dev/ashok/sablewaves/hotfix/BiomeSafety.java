package dev.ashok.sablewaves.hotfix;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
public final class BiomeSafety {
    private BiomeSafety() {}
    public static boolean loadedNeighborhood(Level level, BlockPos pos) {
        int x = pos.getX() >> 4, z = pos.getZ() >> 4;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
            if (!ChunkSafety.loaded(level, (x + dx) << 4, (z + dz) << 4)) return false;
        return true;
    }
}
