package dev.apocalypse.gunsmithingtest;
import java.lang.reflect.*;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import com.rafal.apocalypsegunsmithing.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("gunsmithing_regression")
public class ArmorRegression {
 static int checks;
 public ArmorRegression(){NeoForge.EVENT_BUS.addListener(this::run);}
 static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);checks++;}
 static void near(float got,float wanted,String msg){check(Math.abs(got-wanted)<0.0002F,msg+" got="+got+" expected="+wanted);}
 static float expected(float damage,float armor,float toughness){float reduction=Math.min(20,Math.max(armor*.2F,armor-damage/(2+toughness/4)));return damage*(1-reduction/25);}
 static Zombie diamond(ServerLevel level)throws Exception{
  Zombie z=new Zombie(EntityType.ZOMBIE,level);z.getAttribute(Attributes.ARMOR).setBaseValue(0);z.getAttribute(Attributes.MAX_HEALTH).setBaseValue(120);z.setHealth(120);
  z.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.DIAMOND_HELMET));z.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));z.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.DIAMOND_LEGGINGS));z.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.DIAMOND_BOOTS));Method sync=LivingEntity.class.getDeclaredMethod("detectEquipmentUpdates");sync.setAccessible(true);sync.invoke(z);return z;
 }
 static DamageSource source(ServerLevel level,String id)throws Exception{
  ProjectileEntity p=new ProjectileEntity(EntityType.ARROW,level);p.setItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))));p.setProjectile(new ProjectileConfig());
  Method m=ProjectileEntity.class.getDeclaredMethod("getDamageSource");m.setAccessible(true);return (DamageSource)m.invoke(p);
 }
 void run(ServerStartedEvent event){try{
  ServerLevel level=event.getServer().overworld();
  String[] ids={"cgs:round_revolver_piercing","cgs:round_gatling_piercing","cgs:round_revolver","cgs:round_gatling"};
  for(String id:ids){
   DamageSource s=source(level,id);boolean piercing=id.endsWith("_piercing");
   check((s instanceof PiercingDamageSource)==piercing,id+" exact source filtering");check(s.is(com.nukateam.ntgl.common.foundation.init.NtglDamageTypes.BULLET),id+" retains bullet type");
   for(float armor:new float[]{0,2,7,20,21,22,30})for(float toughness:new float[]{0,8,12})for(float raw:new float[]{1,8,12,24,27,100}){
    near(CombatRules.getDamageAfterAbsorb(null,raw,s,armor,toughness),expected(raw,piercing?armor*.35F:armor,toughness),id+" armor="+armor+" toughness="+toughness+" raw="+raw);
   }
   Zombie z=diamond(level);near(z.getArmorValue(),20,id+" armor");near((float)z.getAttributeValue(Attributes.ARMOR_TOUGHNESS),8,id+" toughness");
   check(z.hurt(s,12),id+" accepted body hit");near(120-z.getHealth(),piercing?10.08F:3.84F,id+" native 12 damage hit");
   check(z.getItemBySlot(EquipmentSlot.CHEST).getDamageValue()==0,id+" ordinary zombie armor durability behavior retained");
   z=diamond(level);check(z.hurt(s,24),id+" accepts 24 damage hit");near(120-z.getHealth(),piercing?22.656F:10.56F,id+" native Sharpshooter IV equivalent hit");
   z=diamond(level);z.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,200,0));check(z.hurt(s,12),id+" Resistance hit");near(120-z.getHealth(),(piercing?10.08F:3.84F)*.8F,id+" Resistance remains effective");
   z=diamond(level);var protection=level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.PROTECTION);
   for(EquipmentSlot slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})z.getItemBySlot(slot).enchant(protection,4);
   check(z.hurt(s,12),id+" Protection hit");near(120-z.getHealth(),(piercing?10.08F:3.84F)*.36F,id+" Protection IV remains effective");
   if(piercing){Zombie natural=diamond(level);natural.getAttribute(Attributes.ARMOR).setBaseValue(2);near(natural.getArmorValue(),22,id+" natural zombie adds two armor");check(natural.hurt(s,24),id+" natural zombie hit");near(120-natural.getHealth(),22.368F,id+" natural zombie diamond damage");}
   if(piercing){z=diamond(level);for(int n=1;n<=6;n++){z.invulnerableTime=0;check(z.hurt(s,24),id+" six-shot sequence hit "+n);check(z.isAlive()==(n<6),id+" six-shot survival "+n);}System.out.println("GUNSMITHING damage "+id+" 12->10.08, 24->22.656, 120HP->6 shots");}
  }
  DamageSource ordinary=source(level,"cgs:round_revolver");
  check(ArmorPenetration.mark(ResourceLocation.parse("other:round_revolver_piercing"),ordinary)==ordinary,"other namespace unaffected");
  check(ArmorPenetration.mark(null,ordinary)==ordinary,"null ammo unaffected");
  check(ArmorPenetration.mark(ResourceLocation.parse("cgs:round_revolver_piercing"),null)==null,"null source unaffected");
  System.out.println("GUNSMITHING REGRESSION PASS checks="+checks);
 }catch(Throwable e){System.out.println("GUNSMITHING REGRESSION FAIL");e.printStackTrace();}event.getServer().halt(false);}
}
