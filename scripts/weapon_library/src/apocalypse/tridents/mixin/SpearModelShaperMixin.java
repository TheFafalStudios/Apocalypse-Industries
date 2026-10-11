package apocalypse.tridents.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.mehvahdjukaar.polytone.Polytone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemModelShaper.class)
public class SpearModelShaperMixin {
    // Both spear GUI rendering hooks re-fetch this model, bypassing ItemRenderer's
    // Polytone selection. Supply the named model at their shared lookup instead.
    @Inject(method = "getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;",
            at = @At("RETURN"), cancellable = true)
    private void preserveNamedSpear(ItemStack stack, CallbackInfoReturnable<BakedModel> cir) {
        if (stack.get(DataComponents.CUSTOM_NAME) == null) return;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (!id.equals("minecraft:netherite_spear") && !id.equals("minecraft:iron_spear")
                && !id.equals("apocalypse_echo_spears:echo_spear")) return;
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        BakedModel model = Polytone.ITEM_MODELS.getOverride(stack, level, null, 0);
        if (model == null) return;
        BakedModel resolved = model.getOverrides().resolve(model, stack, level, null, 0);
        cir.setReturnValue(resolved == null ? model : resolved);
    }
}
