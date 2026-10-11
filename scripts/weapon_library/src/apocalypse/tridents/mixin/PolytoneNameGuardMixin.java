package apocalypse.tridents.mixin;

import java.util.function.Supplier;
import java.util.regex.Pattern;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.mehvahdjukaar.polytone.content.item.ItemModelOverride;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemModelOverride.class, remap = false)
public class PolytoneNameGuardMixin {
    @Shadow @Final protected Pattern pattern;

    // Polytone 4.1.0 skips the name predicate when custom_name is absent.
    // A name_pattern must never replace an unnamed item's vanilla model.
    @Inject(method = "matchesPredicate", at = @At("HEAD"), cancellable = true, remap = false)
    private void requireCustomName(ItemStack stack, Level level, Supplier<CompoundTag> entityTag,
                                   Component name, CallbackInfoReturnable<Boolean> cir) {
        if (pattern != null && name == null) cir.setReturnValue(false);
    }
}
