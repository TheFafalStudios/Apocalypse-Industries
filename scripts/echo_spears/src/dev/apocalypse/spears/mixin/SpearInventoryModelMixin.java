package dev.apocalypse.spears.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.notunanancyowen.spears.Spears;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Select the GUI model before transforms and optimized rendering, rather than swapping only quads. */
@Mixin(value=ItemRenderer.class, priority=1100)
public abstract class SpearInventoryModelMixin {
 @ModifyVariable(method="render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
  at=@At("HEAD"), argsOnly=true, ordinal=0, require=1)
 private BakedModel apocalypse$inventoryModel(BakedModel model, ItemStack stack, ItemDisplayContext context,
  boolean leftHand, PoseStack pose, MultiBufferSource buffers, int light, int overlay, BakedModel original) {
  if ((context==ItemDisplayContext.GUI || context==ItemDisplayContext.GROUND || context==ItemDisplayContext.FIXED)
   && stack.is(Spears.SPEARS)) {
   return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack);
  }
  return model;
 }
}
