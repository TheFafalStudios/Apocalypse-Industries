package dev.apocalypse.subbalance.mixin;
import dev.apocalypse.subbalance.Balance;
import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig;
import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig.HullProperty;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=HullStrengthConfig.class, remap=false)
public abstract class WaterRatings {
    @Inject(method="getFor",at=@At("RETURN"),cancellable=true)
    private static void ai$water(BlockState state, CallbackInfoReturnable<Optional<HullProperty>> cir) {
        cir.setReturnValue(cir.getReturnValue().map(p -> Balance.property(state,p,false,false)));
    }
}
