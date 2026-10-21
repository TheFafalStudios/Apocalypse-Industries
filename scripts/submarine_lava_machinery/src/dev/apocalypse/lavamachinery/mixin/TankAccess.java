package dev.apocalypse.lavamachinery.mixin;
import com.maxenonyme.createsubmarine.submarine.block.entity.BallastTankBlockEntity;
import net.minecraft.core.Direction;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(value=BallastTankBlockEntity.class,remap=false)
public interface TankAccess {
    @Invoker("getCluster") List<BallastTankBlockEntity> ai$cluster();
    @Invoker("checkVentConnection") boolean ai$vent(Direction side);
}
