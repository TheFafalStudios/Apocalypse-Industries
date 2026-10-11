package dev.apocalypse.combat.tests;
import dev.apocalypse.combat.CombatScope;
import com.notunanancyowen.spears.Spears;
import net.bettercombat.BetterCombatMod;
import net.bettercombat.config.ServerConfig;
import net.bettercombat.logic.WeaponRegistry;
import net.bettercombat.api.component.BetterCombatDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import java.lang.reflect.Modifier;
import java.util.Objects;
@Mod("apocalypse_combat_test")
public final class RuntimeTest {
 private static int count=0;
 public RuntimeTest(){NeoForge.EVENT_BUS.addListener(this::test);}
 private static void check(boolean value,String label){if(!value)throw new AssertionError(label);count++;}
 private static ItemStack item(String id){return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)).getDefaultInstance();}
 private void test(ServerStartedEvent event){
  try {
   check(ModList.get().isLoaded("bettercombat"),"Better Combat loaded");
   check(ModList.get().isLoaded("apocalypse_combat_scope"),"scope loaded");
   check(!Spears.hasBetterCombat,"duplicate spear BC bridge disabled by live mixin");
   check(Spears.config.get("spear_stabbing_animation"),"native spear animation retained");
   var defaults=new ServerConfig();
   for(var f:ServerConfig.class.getFields())if(!Modifier.isStatic(f.getModifiers()))check(Objects.deepEquals(f.get(defaults),f.get(BetterCombatMod.config)),"upstream default "+f.getName());
   for(var material:new String[]{"wooden","stone","iron","golden","diamond","netherite"}){
    for(var type:new String[]{"sword","axe"}){
     var stack=item("minecraft:"+material+"_"+type);
     check(CombatScope.allows(stack),"eligible "+material+type);
     var attrs=WeaponRegistry.getAttributes(stack);
     check(attrs!=null && attrs.attacks().length>0,"actual registered profile "+material+type);
    }
   }
   var trident=new ItemStack(Items.TRIDENT);
   check(CombatScope.allows(trident),"trident eligible");
   var tridentAttrs=WeaponRegistry.getAttributes(trident);
   check(tridentAttrs!=null,"trident actual profile registered");
   check(tridentAttrs.category().equals("trident"),"original trident category");
   check(!tridentAttrs.isTwoHanded(),"original one-handed trident profile");
   check(tridentAttrs.rangeBonus()==0.25,"original trident range bonus");
   check(tridentAttrs.attacks().length==1 && tridentAttrs.attacks()[0].animation().equals("bettercombat:one_handed_stab"),"original trident animation");
   trident.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Named Library Trident"));
   check(WeaponRegistry.getAttributes(trident)==tridentAttrs,"named trident keeps same melee profile");
   check(!trident.has(Spears.PIERCING_WEAPON) && !trident.has(Spears.KINETIC_WEAPON),"trident does not become backported spear");
   var swordAttrs=WeaponRegistry.getAttributes(new ItemStack(Items.DIAMOND_SWORD));
   int excluded=0,guns=0,spears=0;
   for(var id:BuiltInRegistries.ITEM.keySet()){
    var stack=item(id.toString());
    if(id.getNamespace().equals("cgs") || id.getNamespace().equals("ntgl") || stack.has(Spears.PIERCING_WEAPON) || stack.has(Spears.KINETIC_WEAPON)
       || id.toString().equals("minecraft:mace") || id.toString().equals("minecraft:diamond_pickaxe")){
     check(!CombatScope.allows(stack),"excluded scope "+id);
     check(WeaponRegistry.getAttributes(stack)==null,"excluded lookup "+id);
     WeaponRegistry.register(id,swordAttrs);
     check(WeaponRegistry.getAttributes(stack)==null,"explicit registration cannot bypass "+id);
     stack.set(BetterCombatDataComponents.WEAPON_PRESET_ID,ResourceLocation.parse("bettercombat:sword"));
     check(WeaponRegistry.getAttributes(stack)==null,"preset component cannot bypass "+id);
     excluded++;
     if(id.getNamespace().equals("cgs"))guns++;
     if(stack.has(Spears.PIERCING_WEAPON))spears++;
    }
   }
   check(guns>0,"real Gunsmithing items tested");check(spears>=7,"backported and Echo spears tested");
   var echo=item("apocalypse_echo_spears:echo_spear");
   var player=FakePlayerFactory.getMinecraft(event.getServer().overworld());
   for(var stack:new ItemStack[]{Spears.NETHERITE_SPEAR.getDefaultInstance(),echo}){
    for(var hand:InteractionHand.values()){
     player.setItemSlot(hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND,stack);
     check(stack.use(event.getServer().overworld(),player,hand).getResult().consumesAction(),"native charge use "+stack+hand);
     check(player.isUsingItem() && player.getUsedItemHand()==hand,"native active charge "+hand);
     check(player.getUseItemRemainingTicks()==72000,"native charge duration");player.stopUsingItem();
    }
   }
   for(var hand:InteractionHand.values()){
    var charged=new ItemStack(Items.TRIDENT);
    player.setItemSlot(hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND,charged);
    check(charged.use(event.getServer().overworld(),player,hand).getResult().consumesAction(),"trident native charge use "+hand);
    check(player.isUsingItem() && player.getUsedItemHand()==hand,"trident native active charge "+hand);
    check(player.getUseItemRemainingTicks()==72000,"trident native charge duration");player.stopUsingItem();
   }
   player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.TRIDENT));
   var level=event.getServer().overworld();
   var area=player.getBoundingBox().inflate(8);
   int projectilesBefore=level.getEntitiesOfClass(net.minecraft.world.entity.projectile.ThrownTrident.class,area).size();
   var thrown=player.getMainHandItem();
   thrown.releaseUsing(level,player,71980);
   check(level.getEntitiesOfClass(net.minecraft.world.entity.projectile.ThrownTrident.class,area).size()==projectilesBefore+1,"native trident release spawns projectile");
   check(WeaponRegistry.getAttributes(new ItemStack(Items.DIAMOND_SWORD))==swordAttrs,"upstream sword profile unchanged");
   check(!CombatScope.allows(ItemStack.EMPTY) && WeaponRegistry.getAttributes(ItemStack.EMPTY)==null,"empty hand stays vanilla");
   System.out.println("COMBAT_SCOPE_TESTS_PASSED checks="+count+" excluded="+excluded+" cgs_items="+guns+" spears="+spears);
  }catch(Throwable error){System.out.println("COMBAT_SCOPE_TESTS_FAILED");error.printStackTrace();}
 }
}
