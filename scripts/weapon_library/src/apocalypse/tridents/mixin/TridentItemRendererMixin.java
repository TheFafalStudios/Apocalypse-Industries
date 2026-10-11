package apocalypse.tridents.mixin;

import apocalypse.tridents.LibraryTridentNames;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.mehvahdjukaar.polytone.Polytone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public class TridentItemRendererMixin {
    private static boolean isLibraryTrident(ItemStack stack) {
        var name = stack.get(DataComponents.CUSTOM_NAME);
        return stack.getItem() == Items.TRIDENT && name != null && LibraryTridentNames.contains(name.getString());
    }

    // Vanilla forces tridents through its entity renderer in hand and replaces
    // their baked model in GUI views. Bypass both branches only for our names.
    @WrapOperation(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean useNamedBakedModel(ItemStack stack, Item item, Operation<Boolean> original) {
        if (item == Items.TRIDENT && isLibraryTrident(stack)) return false;
        return original.call(stack, item);
    }

    // Vanilla's throwing override can otherwise replace Polytone's custom model.
    @Inject(method = "getModel", at = @At("RETURN"), cancellable = true)
    private void resolveLibraryModel(ItemStack stack, Level level, LivingEntity entity, int seed,
                                    CallbackInfoReturnable<BakedModel> cir) {
        if (!isLibraryTrident(stack)) return;
        BakedModel model = Polytone.ITEM_MODELS.getOverride(stack, level, entity, seed);
        if (model == null) return;
        BakedModel resolved = model.getOverrides().resolve(model, stack,
            level instanceof ClientLevel client ? client : null, entity, seed);
        cir.setReturnValue(resolved == null ? model : resolved);
    }
}
