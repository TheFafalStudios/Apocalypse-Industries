package dev.apocalypse.pp.mixin;
import dev.apocalypse.pp.EchoPlate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets={"net.mcreator.protectionpixel.procedures.ConnectheadProcedure","net.mcreator.protectionpixel.procedures.ConnectchestProcedure","net.mcreator.protectionpixel.procedures.ConnectlegProcedure"},remap=false)
public abstract class EchoPlateInstallMixin {
    @Inject(method="execute",at=@At("HEAD"),remap=false)
    private static void echoStats(LevelAccessor world,double x,double y,double z,Entity entity,CallbackInfo ci) {
        if(!world.isClientSide()) EchoPlate.prepareMenu(entity);
    }
}
