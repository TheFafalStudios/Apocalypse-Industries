package apocalypse.tridents.mixin;

import apocalypse.tridents.LibraryTridentNames;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.mehvahdjukaar.polytone.Polytone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTridentRenderer.class)
public abstract class ThrownTridentRendererMixin extends EntityRenderer<ThrownTrident> {
    protected ThrownTridentRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/projectile/ThrownTrident;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD"), cancellable = true)
    private void renderLibraryProjectile(ThrownTrident trident, float yaw, float partialTick,
                                         PoseStack poses, MultiBufferSource buffers, int light,
                                         CallbackInfo ci) {
        var name = trident.getCustomName();
        if (name == null || !LibraryTridentNames.contains(name.getString())) return;

        // The projectile name is synchronized even though its pickup stack is not.
        // Construct a render-only stack; never modify the projectile or real item.
        ItemStack stack = new ItemStack(Items.TRIDENT);
        stack.set(DataComponents.CUSTOM_NAME, name);
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, trident.isFoil());
        var model = Polytone.ITEM_MODELS.getOverride(stack, trident.level(), null, trident.getId());
        if (model == null || model.isCustomRenderer()
            || model == Minecraft.getInstance().getModelManager().getMissingModel()) return;

        poses.pushPose();
        try {
            // Same interpolation and yaw as vanilla. Item geometry points along
            // +Y, opposite to the vanilla entity model, so subtract 180 degrees.
            poses.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, trident.yRotO, trident.getYRot()) - 90));
            poses.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, trident.xRotO, trident.getXRot()) - 90));
            // Match the pack's held third-person size, including model-specific
            // values, rather than imposing one smaller projectile scale.
            var scale = model.getTransforms().getTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).scale;
            poses.scale(scale.x(), scale.y(), scale.z());
            Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.NONE, false,
                poses, buffers, light, OverlayTexture.NO_OVERLAY, model);
        } finally {
            poses.popPose();
        }
        // Keep the base renderer's nameplate and other standard entity behavior.
        super.render(trident, yaw, partialTick, poses, buffers, light);
        ci.cancel();
    }
}
