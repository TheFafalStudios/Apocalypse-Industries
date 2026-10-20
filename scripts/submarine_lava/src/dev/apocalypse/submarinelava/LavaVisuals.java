package dev.apocalypse.submarinelava;
import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
/** Client visual state; never changes fluid physics or the camera fluid type. */
public final class LavaVisuals {
    public static boolean active;
    public static boolean submerged(int y, int top, IntPredicate inside, IntUnaryOperator fluid) {
        if (fluid.applyAsInt(y) == 2) return true;
        for (int i = y; i < top; i++) {
            if (!inside.test(i)) return fluid.applyAsInt(i) == 2;
        }
        return false;
    }
    public static void update() {
        active = false;
        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null || mc.player == null) return;
        var eye = mc.gameRenderer.getMainCamera().getPosition();
        if (CompartmentTracker.findSealedSublevelExact(level, eye) == null) return;
        var pos = BlockPos.containing(eye);
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        var chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
        if (chunk == null) return;
        active = submerged(y, level.getMaxBuildHeight(),
            h -> CompartmentTracker.isOccluded(level, new BlockPos(x, h, z)),
            h -> {
                if (h < level.getMinBuildHeight() || h >= level.getMaxBuildHeight()) return -1;
                // Read the real world fluid, bypassing cabin fluid culling.
                var fluid = CompartmentTracker.realFluidState(chunk, new BlockPos(x, h, z));
                return fluid.is(FluidTags.LAVA) ? 2 : fluid.is(FluidTags.WATER) ? 1 : 0;
            });
    }
}
