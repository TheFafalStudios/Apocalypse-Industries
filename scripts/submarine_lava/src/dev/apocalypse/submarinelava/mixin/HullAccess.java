package dev.apocalypse.submarinelava.mixin;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(targets = "com.maxenonyme.createsubmarine.submarine.compat.iris.ShaderpackSupport", remap = false)
public interface HullAccess {
    @Invoker("hullDepth")
    static int ai$hullDepth() { throw new AssertionError(); }
    @Invoker("outside")
    static DynamicTexture ai$outside() { throw new AssertionError(); }
}
