package dev.apocalypse.pp;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.mcreator.protectionpixel.network.ProtectionPixelModVariables;
import net.mcreator.protectionpixel.procedures.LancerfProcedure;
public final class SelfTest {
 private static int passed;
 private static void check(boolean ok,String message) { if(!ok) throw new IllegalStateException("PP selftest: " + message); passed++; }
 private static ItemStack item(String id) { return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))); }
 private static float attack(net.minecraft.world.entity.player.Player p) {
  var event=new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(p,new net.neoforged.neoforge.common.damagesource.DamageContainer(p.damageSources().playerAttack(p),2));
  net.mcreator.protectionpixel.procedures.Playerhurt3Procedure.onEntityAttacked(event);
  return event.getAmount();
 }
 public static int run(CommandSourceStack source) {
  passed=0;
  var p=FakePlayerFactory.get(source.getLevel() != null ? source.getLevel() : source.getServer().overworld(),new GameProfile(UUID.fromString("5269e941-6313-40e6-89d8-42abf79dfcab"),"[PP-Test]"));
  try {
   p.getInventory().clearContent(); p.removeAllEffects();
   passed+=EchoPlateTest.run(p);
   String[] ids={"protection_pixel:lanceras_helmet","protection_pixel:floatshield_chestplate","protection_pixel:linkplate_leggings","protection_pixel:socks_boots"};
   for(int i=0;i<4;i++) {
    EchoPlateTest.install(p,item(ids[i]),new ItemStack(ApocalypsePP.ECHO_PLATE.get()));
    check(ApocalypsePP.count(p)==i+1,"one intact native Echo per equipped armor slot");
   }
   var v=p.getData(ProtectionPixelModVariables.PLAYER_VARIABLES); v.active=true; v.powerslot=ItemStack.EMPTY;
   ApocalypsePP.sanitize(p); check(!v.active,"stale reactor cleared");
   ApocalypsePP.applyResonance(p); check(!p.hasEffect(MobEffects.DAMAGE_RESISTANCE),"unpowered no buff");
   List<ItemStack> contents=new ArrayList<>(); contents.add(item("protection_pixel:watertank"));
   for(int i=1;i<9;i++) contents.add(i==1?item("protection_pixel:flarerod"):ItemStack.EMPTY);
   v.powerslot=item("protection_pixel:powerengine"); v.powerslot.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(contents)); v.active=true;
   check(ApocalypsePP.workingLoadout(p),"worn PP armor with powered reactor qualifies");
   var worn = new ArrayList<ItemStack>();
   for (var slot : ApocalypsePP.SLOTS) { worn.add(p.getItemBySlot(slot).copy()); p.setItemSlot(slot,ItemStack.EMPTY); }
   p.getInventory().add(item("protection_pixel:lancer_helmet"));
   check(!ApocalypsePP.workingLoadout(p),"carried armor does not qualify");
   p.setItemSlot(ApocalypsePP.SLOTS[0],item("minecraft:diamond_helmet"));
   check(!ApocalypsePP.workingLoadout(p),"foreign worn armor does not qualify");
   p.setItemSlot(ApocalypsePP.SLOTS[0],item("protection_pixel:socks_boots"));
   check(!ApocalypsePP.workingLoadout(p),"wrong armor slot does not qualify");
   p.setItemSlot(ApocalypsePP.SLOTS[0],item("protection_pixel:lancer_helmet"));
   v.active=false;
   check(!ApocalypsePP.workingLoadout(p),"inactive reactor does not qualify");
   v.active=true;
   var loadoutReactor=v.powerslot; v.powerslot=ItemStack.EMPTY;
   check(!ApocalypsePP.workingLoadout(p),"stale active flag does not qualify");
   v.powerslot=loadoutReactor;
   check(ApocalypsePP.workingLoadout(p),"one powered armor piece qualifies");
   for(int slot=0;slot<4;slot++) p.setItemSlot(ApocalypsePP.SLOTS[slot],worn.get(slot));
   check(ApocalypsePP.powered(p),"valid reactor"); ApocalypsePP.applyResonance(p);
   check(p.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier()==1 && p.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier()==1,"full resonance II");
   p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,600,3)); ApocalypsePP.applyResonance(p);
   check(p.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier()==3,"preserve stronger effect");
   EchoPlateTest.recover(p,p.getItemBySlot(ApocalypsePP.SLOTS[3]));
   check(!ApocalypsePP.plated(p.getItemBySlot(ApocalypsePP.SLOTS[3])),"native removal disables boots resonance");
   p.removeAllEffects(); ApocalypsePP.applyResonance(p); check(!p.hasEffect(MobEffects.DAMAGE_RESISTANCE),"three slots no buff");
   ItemStack helmet=p.getItemBySlot(ApocalypsePP.SLOTS[0]); p.tickCount=100; p.setPos(1000,80,1000);
   LancerfProcedure.execute(p.getX(),p.getY(),p.getZ(),p,helmet);
   check(helmet.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getDouble("damage")==0,"mixin first equip reset");
   p.tickCount+=10;p.setPos(1010,80,1000);LancerfProcedure.execute(p.getX(),p.getY(),p.getZ(),p,helmet);
   check(helmet.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getDouble("damage")==6,"mixin cap");
   p.tickCount+=10;p.setPos(2010,80,1000);LancerfProcedure.execute(p.getX(),p.getY(),p.getZ(),p,helmet);
   check(helmet.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getDouble("damage")==0,"mixin teleport reset");
   p.getPersistentData().putDouble("ppexoattack",4);ApocalypsePP.sanitize(p);check(p.getPersistentData().getDouble("ppexoattack")==0,"stale exo reset");
   check(LancerSafety.bounded(Double.NaN)==0 && LancerSafety.bounded(Double.POSITIVE_INFINITY)==0,"nonfinite rejected");
   // Verify recipes survived actual NeoForge/Create deserialization and KubeJS reload.
   try (var stream=SelfTest.class.getResourceAsStream("/apocalypse_pp_recipe_checks.json")) {
    var recipes=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonArray();
    for(var entry:recipes) {
     var row=entry.getAsJsonObject(); var key=ResourceLocation.parse(row.get("recipe").getAsString());
     var holder=source.getServer().getRecipeManager().byKey(key);
     check(holder.isPresent(),"runtime recipe exists "+key);
     check(ApocalypsePP.id(holder.get().value().getResultItem(source.registryAccess())).equals(row.get("output").getAsString()),"runtime recipe output "+key);
    }
   } catch(java.io.IOException ex) { throw new IllegalStateException(ex); }
   CustomData.update(DataComponents.CUSTOM_DATA,helmet,n->n.putDouble("damage",18));
   check(attack(p)==11,"actual Alloy incoming handler: base 2 plus capped 9");
   var brass=item("protection_pixel:lancer_helmet");CustomData.update(DataComponents.CUSTOM_DATA,brass,n->n.putDouble("damage",18));
   p.setItemSlot(ApocalypsePP.SLOTS[0],brass);check(attack(p)==8,"actual Brass incoming handler: base 2 plus capped 6");
   p.setItemSlot(ApocalypsePP.SLOTS[0],helmet);
   var savedReactor=v.powerslot;v.powerslot=ItemStack.EMPTY;v.active=true;
   check(attack(p)==2 && !v.active,"actual damage hook rejects stale power before tick");
   v.powerslot=savedReactor;v.active=true;
   contents.set(0,item("protection_pixel:watertank")); contents.get(0).setDamageValue(2880);
   v.powerslot.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(contents));
   check(!ApocalypsePP.reactorValid(p),"spent water rejected");
   contents.set(0,item("protection_pixel:watertank")); contents.set(1,item("minecraft:diamond"));
   v.powerslot.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(contents));
   check(!ApocalypsePP.reactorValid(p),"automation foreign fuel rejected");
   contents.set(1,ItemStack.EMPTY);v.powerslot.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(contents));
   check(!ApocalypsePP.reactorValid(p),"no fuel rejected");
   p.setItemSlot(ApocalypsePP.SLOTS[3],p.getItemBySlot(ApocalypsePP.SLOTS[0]).copy());
   check(ApocalypsePP.count(p)==3,"wrong armor slot cannot substitute boots");
   passed+=ConsumableRepairTest.run(p);
   source.sendSuccess(()->Component.literal("PP SELFTEST PASS: "+passed+" assertions"),true); return passed;
  } catch(Exception e) { source.sendFailure(Component.literal(e.toString())); throw new IllegalStateException(e); }
  finally { p.getInventory().clearContent(); p.removeAllEffects();p.getData(ProtectionPixelModVariables.PLAYER_VARIABLES).powerslot=ItemStack.EMPTY; }
 }
}
