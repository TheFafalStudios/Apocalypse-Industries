package dev.apocalypse.pp;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.component.DataComponents;
public final class ConsumableRepairRules {
 public static boolean restricted(ItemStack s) {
  String id=ApocalypsePP.id(s);
  return id.equals("protection_pixel:watertank") || id.equals("protection_pixel:flarerod");
 }
 public static ItemEnchantments filter(ItemStack s,ItemEnchantments original) {
  if(!restricted(s) || original.keySet().stream().noneMatch(e->e.is(Enchantments.MENDING))) return original;
  var clean=new ItemEnchantments.Mutable(original);
  clean.removeIf(e->e.is(Enchantments.MENDING));
  return clean.toImmutable();
 }
 public static void clean(ItemStack s) {
  if(!restricted(s))return;
  for(var type:java.util.List.of(DataComponents.ENCHANTMENTS,DataComponents.STORED_ENCHANTMENTS)) {
   var old=s.get(type);
   if(old!=null) {var clean=filter(s,old);if(clean!=old)s.set(type,clean);}
  }
 }
}
