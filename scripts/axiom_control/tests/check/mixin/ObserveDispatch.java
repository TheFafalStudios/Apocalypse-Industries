package check.mixin;
import check.PolicyCheck;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only in the test JAR: observe fall-through, then avoid dispatch on a fake connection. */
@Mixin(value=ServerCommonPacketListenerImpl.class,priority=3000)
public abstract class ObserveDispatch {
    @Inject(method="handleCustomPayload",at=@At("HEAD"),cancellable=true)
    private void check$observe(ServerboundCustomPayloadPacket packet,CallbackInfo ci){
        PolicyCheck.PASSED_DISPATCH++;
        ci.cancel();
    }
}

