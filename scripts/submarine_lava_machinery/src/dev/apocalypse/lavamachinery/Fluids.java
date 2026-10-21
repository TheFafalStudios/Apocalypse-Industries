package dev.apocalypse.lavamachinery;
import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker;
import com.maxenonyme.createsubmarine.submarine.util.SubLevelRegistry;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.*;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3d;
public final class Fluids {
    public static boolean supported(Fluid fluid) {
        return fluid==net.minecraft.world.level.material.Fluids.WATER || fluid==net.minecraft.world.level.material.Fluids.FLOWING_WATER
            || fluid==net.minecraft.world.level.material.Fluids.LAVA || fluid==net.minecraft.world.level.material.Fluids.FLOWING_LAVA
            || fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA);
    }
    public static boolean supported(FluidState state) { return !state.isEmpty() && supported(state.getType()); }
    public static boolean supported(FluidStack stack) { return !stack.isEmpty() && supported(stack.getFluid()); }
    public static Fluid source(Fluid fluid) { return fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid; }
    /** Fresh, non-generating world-space sample; sealed cabin masking is not ocean fluid. */
    public static FluidState outside(Level level,BlockPos pos) {
        if(level==null)return net.minecraft.world.level.material.Fluids.EMPTY.defaultFluidState();
        var access=SableCompanion.INSTANCE.getContaining(level,pos);
        if(access!=null) {
            var point=new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5);
            access.logicalPose().transformPosition(point);
            pos=BlockPos.containing(point.x,point.y,point.z);
            level=access instanceof SubLevel sub ? sub.getLevel() : SubLevelRegistry.getLevel(access.getUniqueId());
        }
        if(level==null || !level.hasChunkAt(pos))return net.minecraft.world.level.material.Fluids.EMPTY.defaultFluidState();
        return CompartmentTracker.realFluidState(level,pos);
    }
}
