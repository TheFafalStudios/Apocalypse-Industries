package dev.apocalypse.axiomcontrol.mixin;
import dev.apocalypse.axiomcontrol.AxiomControl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=ServerCommonPacketListenerImpl.class,priority=2000)
public abstract class AxiomPayloads {
    @Shadow @Final protected MinecraftServer server;
    @Inject(method="handleCustomPayload",at=@At("HEAD"),cancellable=true)
    private void apocalypse$gate(ServerboundCustomPayloadPacket packet,CallbackInfo ci){
        if(!AxiomControl.allowed(server) && packet.payload().type().id().getNamespace().equals("axiom")) {
            AxiomControl.recordBlocked(); ci.cancel();
        }
    }
}
