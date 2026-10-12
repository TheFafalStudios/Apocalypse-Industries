package check.mixin;
import check.PhantomCheck;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Deterministic equivalent of mipmap completion after capturing a one-level array. */
@Mixin(SpriteContents.class)
public abstract class RaceFixture {
    @ModifyVariable(method="upload",at=@At("HEAD"),argsOnly=true,ordinal=0)
    private NativeImage[] check$capturedBeforeMipmapCompletion(NativeImage[] images){
        var sprite=(SpriteContents)(Object)this;
        if(PhantomCheck.fixture && Minecraft.getInstance().getOverlay() instanceof LoadingOverlay
            && sprite.name().getNamespace().equals("railways")
            && sprite.name().getPath().startsWith("block/track/phantom/") && images.length>1){
            return new NativeImage[]{images[0]};
        }
        return images;
    }
}
