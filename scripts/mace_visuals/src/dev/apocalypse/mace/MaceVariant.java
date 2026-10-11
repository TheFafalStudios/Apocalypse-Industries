package dev.apocalypse.mace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.component.DataComponents;
public final class MaceVariant {
 private MaceVariant() {}
 public static float value(ItemStack stack) {
  if (!stack.is(Items.MACE)) return 0;
  var enchants=stack.get(DataComponents.ENCHANTMENTS);
  if (enchants==null) return 0;
  boolean breach=false;
  for (var entry:enchants.entrySet()) {
   if (entry.getIntValue()<=0) continue;
   if (entry.getKey().is(Enchantments.WIND_BURST)) return 2;
   if (entry.getKey().is(Enchantments.BREACH)) breach=true;
  }
  return breach ? 1 : 0;
 }
}
