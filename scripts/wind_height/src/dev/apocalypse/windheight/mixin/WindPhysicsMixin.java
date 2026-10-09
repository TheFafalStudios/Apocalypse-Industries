package dev.apocalypse.windheight.mixin;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.wind.WindManager;

/** Reuse Weather2's positional sampling once per ship, at its existing force origin. */
@Mixin(targets = "net.killey.tornadophysics.logic.WindPhysics", remap = false)
public abstract class WindPhysicsMixin {
    @Redirect(
        method = "processGlobalWind(Ldev/ryanhcode/sable/sublevel/ServerSubLevel;Lweather2/weathersystem/WeatherManagerServer;Lnet/minecraft/server/level/ServerLevel;I)V",
        at = @At(value = "INVOKE", target = "Lweather2/weathersystem/wind/WindManager;getWindSpeed()F"),
        require = 1, expect = 1, allow = 1, remap = false)
    private static float apocalypse$positionalWind(WindManager wind, ServerSubLevel ship,
            WeatherManagerServer weather, ServerLevel level, int tick) {
        var position = ship.logicalPose().position();
        return wind.getWindSpeed(BlockPos.containing(position.x(), position.y(), position.z()));
    }
}
