package dev.apocalypse.combat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import com.notunanancyowen.spears.Spears;
/** Eligibility only: upstream weapon profiles and balance remain unchanged. */
public final class CombatScope {
 private CombatScope() {}
 public static boolean allows(ItemStack stack) {
  if(stack==null || stack.isEmpty()) return false;
  String ns=BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
  if(ns.equals("cgs") || ns.equals("ntgl") || ns.equals("spears") || ns.equals("apocalypse_echo_spears")) return false;
  if(stack.has(Spears.PIERCING_WEAPON) || stack.has(Spears.KINETIC_WEAPON)) return false;
  return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem || stack.getItem() instanceof TridentItem;
 }
}
