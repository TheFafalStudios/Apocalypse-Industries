package dev.apocalypse.axiomcontrol.mixin;

import dev.apocalypse.axiomcontrol.AxiomControl;
import com.moulberry.axiom.AxiomServer;
import com.moulberry.axiom.restrictions.AxiomPermission;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Set;
import java.util.UUID;

@Mixin(value=AxiomServer.class,remap=false)
public abstract class AxiomAuthorization {
    @Shadow @Final public Set<UUID> sentDisableReasonPlayers;
    @Inject(method="hasPermission",at=@At("HEAD"),cancellable=true,remap=false)
    private static void apocalypse$permission(ServerPlayer player,AxiomPermission permission,CallbackInfoReturnable<Boolean> ci) {
        if(!AxiomControl.allowed(player.server))ci.setReturnValue(false);
    }
    @Inject(method="canUseAxiom(Lnet/minecraft/server/level/ServerPlayer;)Z",at=@At("HEAD"),cancellable=true,remap=false)
    private static void apocalypse$use(ServerPlayer player,CallbackInfoReturnable<Boolean> ci) {
        if(!AxiomControl.allowed(player.server))ci.setReturnValue(false);
    }
    @Inject(method="canUseAxiom(Lnet/minecraft/server/level/ServerPlayer;Lcom/moulberry/axiom/restrictions/AxiomPermission;)Z",at=@At("HEAD"),cancellable=true,remap=false)
    private static void apocalypse$usePermission(ServerPlayer player,AxiomPermission permission,CallbackInfoReturnable<Boolean> ci) {
        if(!AxiomControl.allowed(player.server))ci.setReturnValue(false);
    }
    @Inject(method="acceptHandshake",at=@At("HEAD"),cancellable=true,remap=false)
    private static void apocalypse$handshake(ServerPlayer player,long id,CallbackInfoReturnable<Boolean> ci) {
        if(!AxiomControl.allowed(player.server))ci.setReturnValue(false);
    }
    @Inject(method="lambda$register$0",at=@At("HEAD"),remap=false)
    private static void apocalypse$retryHandshake(MinecraftServer server,CallbackInfo ci) {
        // Clearing the native denial cache lets a later enable command start a fresh handshake.
        if(AxiomControl.allowed(server)) {
            ((com.moulberry.axiom.hooks.MinecraftServerExt)server).axiom$getAxiomServer().sentDisableReasonPlayers.clear();
        }
    }
}
