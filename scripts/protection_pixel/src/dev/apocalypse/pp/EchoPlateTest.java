package dev.apocalypse.pp;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.mcreator.protectionpixel.init.ProtectionPixelModMenus;
import net.mcreator.protectionpixel.world.inventory.*;
import net.mcreator.protectionpixel.procedures.*;

public final class EchoPlateTest {
 private static int passed;
 private static void check(boolean ok,String label) {if(!ok)throw new IllegalStateException("Echo native plate: "+label);passed++;}
 private static ItemStack item(String id){return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)));}
 public static ProtectionPixelModMenus.MenuAccessor menu(ServerPlayer p,ItemStack armor) {
  AbstractContainerMenu m=switch(((ArmorItem)armor.getItem()).getEquipmentSlot()) {
   case CHEST -> new ChestguiMenu(101,p.getInventory(),null);
   case LEGS -> new LegguiMenu(101,p.getInventory(),null);
   default -> new HeadguiMenu(101,p.getInventory(),null);
  };
  p.containerMenu=m;
  var access=(ProtectionPixelModMenus.MenuAccessor)m;access.getSlots().get(0).set(armor.copy());return access;
 }
 public static ItemStack install(ServerPlayer p,ItemStack armor,ItemStack... plates) {
  p.setGameMode(GameType.CREATIVE);
  var slot=((ArmorItem)armor.getItem()).getEquipmentSlot();p.setItemSlot(slot,armor);
  var m=menu(p,armor);
  for(int i=0;i<plates.length;i++){
   check(m.getSlots().get(i+1).mayPlace(plates[i]),"native slot accepts "+ApocalypsePP.id(plates[i]));
   m.getSlots().get(i+1).set(plates[i]);
  }
  switch(slot) {
   case CHEST -> ConnectchestProcedure.execute(p.level(),p.getX(),p.getY(),p.getZ(),p);
   case LEGS -> ConnectlegProcedure.execute(p.level(),p.getX(),p.getY(),p.getZ(),p);
   default -> ConnectheadProcedure.execute(p.level(),p.getX(),p.getY(),p.getZ(),p);
  }
  return p.getItemBySlot(slot);
 }
 public static ProtectionPixelModMenus.MenuAccessor recover(ServerPlayer p,ItemStack armor) throws Exception {
  var m=menu(p,armor);
  // Invoke the native queued body synchronously; the logic is unchanged.
  var method=BreakarmorProcedure.class.getDeclaredMethod("lambda$execute$4",Entity.class,LevelAccessor.class,double.class,double.class,double.class);
  method.setAccessible(true);method.invoke(null,p,p.level(),p.getX(),p.getY(),p.getZ());return m;
 }
 public static int run(ServerPlayer p) throws Exception {
  passed=0;
  var echo=item("apocalypse_pp:echo_plate");
  check(echo.getMaxDamage()==100 && echo.getMaxStackSize()==1,"100 durability; unstackable");
  String[] armors={"protection_pixel:hunter_helmet","protection_pixel:floatshield_chestplate","protection_pixel:linkplate_leggings","protection_pixel:socks_boots","protection_pixel:linkplate_helmet","protection_pixel:linkplate_chestplate","protection_pixel:linkplate_boots"};
  for(String id:armors){
   var armor=item(id);armor.setDamageValue(12);
   CustomData.update(DataComponents.CUSTOM_DATA,armor,n->{n.putString("unrelated","preserve");n.putDouble("basicdurable",12);});
   var plate=item("apocalypse_pp:echo_plate");plate.setDamageValue(20);
   // Force wrong cached stats: server connect hook must supply current Alloy config.
   CustomData.update(DataComponents.CUSTOM_DATA,plate,n->n.putDouble("armor",77));
   armor=install(p,armor,plate);
   var n=armor.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
   check(n.getString("slot1").equals("apocalypse_pp:echo_plate") && n.getDouble("s1")==20,"native stored plate identity/wear "+id);
   check(n.getDouble("platearmor")==1.5 && n.getDouble("platetoughness")==1 && n.getDouble("weight")==1.5,"Alloy-equivalent native stats "+id);
   check(n.getString("unrelated").equals("preserve") && armor.getDamageValue()==12,"unrelated components preserved "+id);
   check(ApocalypsePP.plated(armor),"intact native Echo detected "+id);
   armor.setDamageValue(90);check(ApocalypsePP.plated(armor),"last serviceable wear unit "+id);
   armor.setDamageValue(91);check(!ApocalypsePP.plated(armor),"exhaustion disables Echo "+id);
   armor.setDamageValue(90); // Recover before the native random exhaustion-loss roll.
   var recovered=recover(p,armor).getSlots().get(1).getItem();
   check(recovered.is(ApocalypsePP.ECHO_PLATE.get()) && recovered.getDamageValue()==98,"native recovery preserves wear "+id);
  }
  var armor=item("protection_pixel:hunter_helmet");
  armor=install(p,armor,item("protection_pixel:alloyarmorplate"),item("apocalypse_pp:echo_plate"));
  armor.setDamageValue(99);check(ApocalypsePP.plated(armor),"first Alloy wears out before second Echo");
  armor.setDamageValue(197);check(ApocalypsePP.plated(armor),"second plate last serviceable wear unit");
  armor.setDamageValue(198);check(!ApocalypsePP.plated(armor),"both plates exhausted");
  armor.setDamageValue(197);
  var recovered=recover(p,armor);var plate=recovered.getSlots().get(2).getItem();
  check(plate.getDamageValue()==98,"native sequential recovery agrees with resonance");
  var repair=new PlaterepairMenu(102,p.getInventory(),null);p.containerMenu=repair;
  ((ProtectionPixelModMenus.MenuAccessor)repair).getSlots().get(0).set(plate);
  var kit=item("protection_pixel:armorplatekit");p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,kit);
  p.setGameMode(GameType.SURVIVAL);
  RepairProcedure.execute(p.level(),p.getX(),p.getY(),p.getZ(),p);
  check(plate.getDamageValue()==0,"native kit repairs worn Echo");
  check(kit.getDamageValue()==98,"repair consumes corresponding kit durability");
  var foreign=item("minecraft:diamond_helmet");CustomData.update(DataComponents.CUSTOM_DATA,foreign,n->n.putString("slot1","apocalypse_pp:echo_plate"));
  check(!ApocalypsePP.plated(foreign),"foreign armor rejected");
  var marker=item("protection_pixel:hunter_helmet");CustomData.update(DataComponents.CUSTOM_DATA,marker,n->n.putBoolean("apocalypse_echo_plate",true));
  check(!ApocalypsePP.plated(marker),"legacy marker grants no resonance; no migration");
  p.containerMenu=p.inventoryMenu;p.getInventory().clearContent();
  return passed;
 }
}
