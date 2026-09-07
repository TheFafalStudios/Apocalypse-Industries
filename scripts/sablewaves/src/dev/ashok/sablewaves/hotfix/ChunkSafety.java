package dev.ashok.sablewaves.hotfix;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
/** Read loaded server terrain without scheduling or waiting for chunks. */
public final class ChunkSafety {
    private ChunkSafety() {}
    public static boolean loaded(Level level, int x, int z) {
        if (level instanceof ServerLevel server)
            return server.getChunkSource().getChunkNow(x >> 4, z >> 4) != null;
        return level.hasChunk(x >> 4, z >> 4);
    }
    public static FluidState fluid(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server) {
            LevelChunk chunk = server.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            return chunk == null ? Fluids.EMPTY.defaultFluidState() : chunk.getFluidState(pos);
        }
        return level.getFluidState(pos);
    }
}
