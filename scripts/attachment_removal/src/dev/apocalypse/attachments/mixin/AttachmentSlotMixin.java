package dev.apocalypse.attachments.mixin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Retain NTGL inventory and ammunition handling, removing only its creative gate. */
@Mixin(targets="com.nukateam.ntgl.common.foundation.container.slot.AttachmentSlot", remap=false)
public abstract class AttachmentSlotMixin {
    @Redirect(method="mayPickup(Lnet/minecraft/world/entity/player/Player;)Z",
        at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;isCreative()Z"),
        require=1, expect=1, allow=1, remap=false)
    private boolean apocalypseAttachments$allowRemoval(Player player) {
        return !player.isSpectator();
    }
    // NTGL refunds the entire excess as one stack. Split it before inventory/drop
    // handling so a full inventory cannot produce an unsaveable oversized entity.
    @Redirect(method="checkAmmoCount(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V",
        at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;addItem(Lnet/minecraft/world/item/ItemStack;)Z"),
        require=1, expect=1, allow=1, remap=false)
    private static boolean apocalypseAttachments$returnValidStacks(Player player, ItemStack excess) {
        while (!excess.isEmpty()) {
            ItemStack part = excess.split(Math.max(1, Math.min(99, excess.getMaxStackSize())));
            if (!player.addItem(part) && !part.isEmpty()) player.drop(part, false);
        }
        return true; // All parts returned; suppress NTGL's unsplit fallback drop.
    }
}
