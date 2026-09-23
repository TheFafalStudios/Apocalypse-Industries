package dev.apocalypse.pp.mixin;
import dev.apocalypse.pp.ConsumableRepairRules;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(targets={"net.mcreator.protectionpixel.item.WatertankItem","net.mcreator.protectionpixel.item.FlarerodItem"},remap=false)
public abstract class ConsumableItemMixin extends Item {
 protected ConsumableItemMixin(){super(new Item.Properties());}
 @Override public boolean supportsEnchantment(ItemStack stack,Holder<Enchantment> enchantment) {
  return !enchantment.is(Enchantments.MENDING) && super.supportsEnchantment(stack,enchantment);
 }
 @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected) {
  super.inventoryTick(stack,level,entity,slot,selected);
  if(!level.isClientSide())ConsumableRepairRules.clean(stack);
 }
}
