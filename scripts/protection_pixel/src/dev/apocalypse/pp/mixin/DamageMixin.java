package dev.apocalypse.pp.mixin;
import dev.apocalypse.pp.ApocalypsePP;
import net.mcreator.protectionpixel.procedures.Playerhurt3Procedure;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Playerhurt3Procedure.class, remap=false)
public class DamageMixin {
 @Inject(method="onEntityAttacked", at=@At("HEAD"), remap=false)
 private static void sanitize(LivingIncomingDamageEvent e,CallbackInfo ci) { if(e.getSource().getEntity() instanceof Player p) ApocalypsePP.sanitize(p); }
}
