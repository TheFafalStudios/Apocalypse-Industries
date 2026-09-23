package dev.apocalypse.pp;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerPlayer;
public final class ConsumableRepairTest {
 private static int checks;
 private static void check(boolean condition,String message) {
  if(!condition)throw new IllegalStateException("Consumable repair: "+message);checks++;
 }
 private static ItemStack item(String id) {return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)));}
 public static int run(ServerPlayer p) {
  checks=0;
  try {
   p.getInventory().clearContent();
   for(var slot:ApocalypsePP.SLOTS)p.setItemSlot(slot,ItemStack.EMPTY);
   var registry=p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
   var mending=registry.getOrThrow(Enchantments.MENDING);
   var unbreaking=registry.getOrThrow(Enchantments.UNBREAKING);
   var mixed=new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
   mixed.set(mending,1);mixed.set(unbreaking,2);var original=mixed.toImmutable();
   for(String id:new String[]{"protection_pixel:watertank","protection_pixel:flarerod"}) {
    var s=item(id);s.setDamageValue(42);
    check(!s.supportsEnchantment(mending),id+" rejects normal Mending eligibility");
    s.enchant(mending,1);
    check(s.getEnchantments().getLevel(mending)==0,id+" rejects direct enchant");
    EnchantmentHelper.setEnchantments(s,original);
    check(s.getEnchantments().getLevel(mending)==0,id+" rejects helper/machine application");
    check(s.getEnchantments().getLevel(unbreaking)==2,id+" preserves other enchantments");
    s.set(DataComponents.ENCHANTMENTS,original);
    check(s.get(DataComponents.ENCHANTMENTS).getLevel(mending)==0,id+" blocks raw component write");
    check(s.getDamageValue()==42,id+" enchant filtering preserves spent durability");
    s.set(DataComponents.STORED_ENCHANTMENTS,original);
    check(s.get(DataComponents.STORED_ENCHANTMENTS).getLevel(mending)==0,id+" blocks stored-enchantment bypass");
    var legacy=new ItemStack(s.getItem().builtInRegistryHolder(),1,DataComponentPatch.builder().set(DataComponents.ENCHANTMENTS,original).set(DataComponents.DAMAGE,42).build());
    check(legacy.get(DataComponents.ENCHANTMENTS).getLevel(mending)==1,id+" legacy fixture bypasses normal setter");
    check(legacy.getAllEnchantments(registry).getLevel(mending)==0,id+" loaded enchantment ineffective");
    check(legacy.getDamageValue()==42 && legacy.getEnchantments().getLevel(unbreaking)==2,id+" legacy cleanup preserves damage and other enchants");
    p.setItemSlot(EquipmentSlot.MAINHAND,legacy);
    check(EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP,p,x->true).isEmpty(),id+" cannot be selected for XP Mending");
    var tickLegacy=new ItemStack(s.getItem().builtInRegistryHolder(),1,DataComponentPatch.builder().set(DataComponents.ENCHANTMENTS,original).build());
    tickLegacy.getItem().inventoryTick(tickLegacy,p.level(),p,0,false);
    check(tickLegacy.get(DataComponents.ENCHANTMENTS).getLevel(mending)==0,id+" inventory tick removes existing Mending");
    s.setDamageValue(43);check(s.getDamageValue()==43,id+" ordinary fuel/water wear still works");
   }
   var pick=item("minecraft:diamond_pickaxe");pick.setDamageValue(42);pick.enchant(mending,1);
   check(pick.supportsEnchantment(mending) && pick.getEnchantments().getLevel(mending)==1,"normal equipment retains Mending");
   p.setItemSlot(EquipmentSlot.MAINHAND,pick);
   check(EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP,p,x->true).isPresent(),"normal Mending still selects tools");
   var book=item("minecraft:enchanted_book");book.set(DataComponents.STORED_ENCHANTMENTS,original);
   check(book.get(DataComponents.STORED_ENCHANTMENTS).getLevel(mending)==1,"Mending books preserved");
   p.getInventory().clearContent();
   var water=item("protection_pixel:watertank");water.setDamageValue(42);
   var rod=item("protection_pixel:flarerod");rod.setDamageValue(42);
   p.getInventory().setItem(0,water);p.getInventory().setItem(1,rod);p.getInventory().setItem(2,pick);
   p.tickCount=0;
   Class<?> context=Class.forName("top.theillusivec4.curios.api.SlotContext");
   var constructor=context.getConstructors()[0];Object[] args=new Object[constructor.getParameterCount()];
   for(int i=0;i<args.length;i++) {
    Class<?> t=constructor.getParameterTypes()[i];
    if(t==String.class)args[i]="charm";else if(t==int.class)args[i]=0;else if(t==boolean.class)args[i]=false;else if(t.isInstance(p))args[i]=p;else throw new IllegalStateException("Unknown SlotContext parameter "+t);
   }
   var reforger=item("nameless_trinkets:reforger");
   check(!reforger.isEmpty(),"Reforger item exists");
   reforger.getItem().getClass().getMethod("curioTick",context,ItemStack.class).invoke(reforger.getItem(),constructor.newInstance(args),reforger);
   check(water.getDamageValue()==42,"actual Reforger tick skips water tank");
   check(rod.getDamageValue()==42,"actual Reforger tick skips flare rod");
   check(pick.getDamageValue()<42,"actual Reforger tick still repairs normal tool");
   return checks;
  } catch(Exception e) {throw new IllegalStateException("Consumable repair test failed",e);}
  finally {p.getInventory().clearContent();}
 }
}
