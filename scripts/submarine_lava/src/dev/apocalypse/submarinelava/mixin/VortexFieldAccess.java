package dev.apocalypse.submarinelava.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(targets="com.maxenonyme.vortexengine.light.LightField",remap=false)
public interface VortexFieldAccess {
 @Accessor("originX") static int ai$x(){throw new AssertionError();}
 @Accessor("originY") static int ai$y(){throw new AssertionError();}
 @Accessor("originZ") static int ai$z(){throw new AssertionError();}
 @Accessor("mode") static int ai$mode(){throw new AssertionError();}
}
