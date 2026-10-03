package dev.apocalypse.attachmenttest;
import java.util.*;
import com.mojang.authlib.GameProfile;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.foundation.container.AttachmentContainer;
import com.nukateam.ntgl.common.foundation.container.slot.AttachmentSlot;
import com.nukateam.ntgl.common.util.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.inventory.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("attachment_regression")
public class AttachmentRegression {
 static int checks, combinations;
 public AttachmentRegression(){NeoForge.EVENT_BUS.addListener(this::run);}
 static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);checks++;}
 static Item item(String id){return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));}
 static int count(FakePlayer p,Item item){int n=0;for(ItemStack s:p.getInventory().items)if(s.is(item))n+=s.getCount();return n;}
 static AttachmentContainer menu(FakePlayer p,ItemStack gun){p.getInventory().selected=0;p.getInventory().setItem(0,gun);return new AttachmentContainer(1,p.getInventory(),gun);}
 static AttachmentSlot slot(AttachmentContainer m,ItemStack a){for(Slot s:m.slots)if(s instanceof AttachmentSlot as && as.mayPlace(a))return as;return null;}
 static void exercise(ServerLevel level,FakePlayer p,Item gunItem,Item attachment,boolean shift){
  p.setGameMode(GameType.SURVIVAL);p.getInventory().clearContent();ItemStack gun=new ItemStack(gunItem),a=new ItemStack(attachment);
  AttachmentContainer m=menu(p,gun);AttachmentSlot s=slot(m,a);check(s!=null,"compatible slot");String label=BuiltInRegistries.ITEM.getKey(gunItem)+"/"+BuiltInRegistries.ITEM.getKey(attachment)+(shift?" shift":" pickup");
  int base=WeaponModifierHelper.getMaxAmmo(new WeaponData(gun,p));
  m.setCarried(a.copy());m.clicked(s.index,0,ClickType.PICKUP,p);check(s.getItem().is(attachment),label+" attached");check(m.getCarried().isEmpty(),label+" consumed carried once");
  check(WeaponStateHelper.hasAttachmentEquipped(gun,s.getType()),label+" persisted attached");
  ItemStack saved=ItemStack.parse(level.registryAccess(),gun.save(level.registryAccess())).orElseThrow();check(WeaponStateHelper.hasAttachmentEquipped(saved,s.getType()),label+" save roundtrip attached");
  p.setGameMode(GameType.SPECTATOR);check(!s.mayPickup(p),label+" spectator denied");p.setGameMode(GameType.CREATIVE);check(s.mayPickup(p),label+" creative allowed");p.setGameMode(GameType.ADVENTURE);check(s.mayPickup(p),label+" adventure allowed");p.setGameMode(GameType.SURVIVAL);check(s.mayPickup(p),label+" survival allowed");
  WeaponData data=new WeaponData(gun,p);int max=WeaponModifierHelper.getMaxAmmo(data);int loaded=Math.min(base,max);WeaponStateHelper.setAmmoCount(data,loaded);
  if(shift){m.clicked(s.index,0,ClickType.QUICK_MOVE,p);check(count(p,attachment)==1,label+" inventory returned exactly once");check(m.getCarried().isEmpty(),label+" no carried duplicate");}
  else{m.clicked(s.index,0,ClickType.PICKUP,p);check(m.getCarried().is(attachment)&&m.getCarried().getCount()==1,label+" cursor returned exactly once");check(count(p,attachment)==0,label+" no inventory duplicate");m.removed(p);check(count(p,attachment)==1,label+" closing returns cursor");}
  check(s.getItem().isEmpty(),label+" slot empty");check(!WeaponStateHelper.hasAttachmentEquipped(gun,s.getType()),label+" removed in gun state");check(WeaponStateHelper.getAmmoCount(data)==loaded,label+" loaded ammo preserved");
  ItemStack restored=ItemStack.parse(level.registryAccess(),gun.save(level.registryAccess())).orElseThrow();check(!WeaponStateHelper.hasAttachmentEquipped(restored,s.getType()),label+" save roundtrip removed");
  AttachmentContainer reopened=menu(p,restored);for(Slot rs:reopened.slots)if(rs instanceof AttachmentSlot as && as.getType().equals(s.getType()))check(as.getItem().isEmpty(),label+" reopened absent");
  for(int n=0;n<3;n++){m.clicked(s.index,0,ClickType.PICKUP,p);check(m.getCarried().isEmpty(),label+" repeat empty no duplication");}check(count(p,attachment)==1,label+" conserved attachment");combinations++;
 }
 static void magazine(ServerLevel level,FakePlayer p){
  p.setGameMode(GameType.SURVIVAL);p.getInventory().clearContent();ItemStack gun=new ItemStack(item("cgs:gatling")),drum=new ItemStack(item("cgs:gatling_drum"));AttachmentContainer m=menu(p,gun);AttachmentSlot s=slot(m,drum);check(s!=null,"drum slot");
  int base=WeaponModifierHelper.getMaxAmmo(new WeaponData(gun,p));m.setCarried(drum);m.clicked(s.index,0,ClickType.PICKUP,p);WeaponData data=new WeaponData(gun,p);int expanded=WeaponModifierHelper.getMaxAmmo(data);check(expanded>base,"drum expands capacity");
  WeaponStateHelper.setCurrentAmmo(data,ResourceLocation.parse("cgs:round_gatling"));WeaponStateHelper.setAmmoCount(data,expanded);m.clicked(s.index,0,ClickType.PICKUP,p);
  check(WeaponStateHelper.getAmmoCount(data)==base,"removing loaded drum clamps ammo");check(count(p,item("cgs:round_gatling"))==expanded-base,"excess rounds refunded exactly");check(m.getCarried().is(item("cgs:gatling_drum")),"drum returned");m.removed(p);check(count(p,item("cgs:gatling_drum"))==1,"one drum in inventory");System.out.println("ATTACHMENT magazine "+expanded+" -> "+base+"; refunded="+(expanded-base));
 }
 static void edgeCases(ServerLevel level,FakePlayer p){
  p.setGameMode(GameType.SURVIVAL);p.getInventory().clearContent();ItemStack gun=new ItemStack(item("cgs:revolver")),scope=new ItemStack(item("cgs:scope"));AttachmentContainer m=menu(p,gun);AttachmentSlot s=slot(m,scope);m.setCarried(scope);m.clicked(s.index,0,ClickType.PICKUP,p);
  for(int n=1;n<36;n++)p.getInventory().setItem(n,new ItemStack(Items.STONE,64));
  m.clicked(s.index,0,ClickType.QUICK_MOVE,p);check(s.getItem().is(item("cgs:scope")),"full inventory shift retains scope");check(WeaponStateHelper.hasAttachmentEquipped(gun,s.getType()),"full inventory retains gun attachment");
  m.setCarried(new ItemStack(Items.DIRT));m.clicked(s.index,0,ClickType.PICKUP,p);check(s.getItem().is(item("cgs:scope")),"incompatible swap rejected");check(m.getCarried().is(Items.DIRT),"incompatible item retained on cursor");m.setCarried(ItemStack.EMPTY);
  m.clicked(s.index,0,ClickType.PICKUP,p);check(m.getCarried().is(item("cgs:scope")),"full inventory normal pickup works");m.removed(p);
  int drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(32)).stream().filter(e->e.getItem().is(item("cgs:scope"))).mapToInt(e->e.getItem().getCount()).sum();check(drops==1,"full inventory closing drops exactly one scope");
  for(var e:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(32)))e.discard();
  p.getInventory().clearContent();gun=new ItemStack(item("cgs:gatling"));m=menu(p,gun);s=slot(m,new ItemStack(item("cgs:gatling_drum")));m.setCarried(new ItemStack(item("cgs:gatling_drum")));m.clicked(s.index,0,ClickType.PICKUP,p);WeaponData data=new WeaponData(gun,p);int max=WeaponModifierHelper.getMaxAmmo(data);WeaponStateHelper.setCurrentAmmo(data,ResourceLocation.parse("cgs:round_gatling"));WeaponStateHelper.setAmmoCount(data,max);
  for(int n=1;n<36;n++)p.getInventory().setItem(n,new ItemStack(Items.STONE,64));m.clicked(s.index,0,ClickType.PICKUP,p);
  int rounds=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(32)).stream().filter(e->e.getItem().is(item("cgs:round_gatling"))).mapToInt(e->e.getItem().getCount()).sum();check(rounds+WeaponStateHelper.getAmmoCount(data)==max,"full inventory ammo conserved via drops");check(rounds==200,"overflow ammo drops exactly 200");
  for(var e:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(32))) {
   ItemStack stack=e.getItem();check(stack.getCount()<=stack.getMaxStackSize(),"dropped stack within item limit");ItemStack persisted=ItemStack.parse(level.registryAccess(),stack.save(level.registryAccess())).orElseThrow();check(ItemStack.isSameItemSameComponents(stack,persisted)&&stack.getCount()==persisted.getCount(),"dropped stack survives serialization");
  }
m.removed(p);
  int drums=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(32)).stream().filter(e->e.getItem().is(item("cgs:gatling_drum"))).mapToInt(e->e.getItem().getCount()).sum();check(drums==1,"full inventory closing drops exactly one drum");
  p.getInventory().clearContent();AttachmentContainer normal=menu(p,new ItemStack(item("cgs:revolver")));Slot ordinary=normal.slots.stream().filter(x->!(x instanceof AttachmentSlot)&&x.mayPickup(p)).findFirst().orElseThrow();ordinary.set(new ItemStack(Items.DIRT,5));normal.clicked(ordinary.index,0,ClickType.PICKUP,p);check(normal.getCarried().is(Items.DIRT)&&normal.getCarried().getCount()==5,"ordinary inventory pickup unchanged");normal.removed(p);
 }
 void run(ServerStartedEvent event){try{ServerLevel level=event.getServer().overworld();FakePlayer p=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("efa836f4-48ee-44cd-a001-c628f934f001"),"AttachmentTest"));
  List<Item> attachments=new ArrayList<>();for(Item i:BuiltInRegistries.ITEM)if(i instanceof IAttachment)attachments.add(i);
  for(Item gun:BuiltInRegistries.ITEM)if(gun instanceof IWeapon && BuiltInRegistries.ITEM.getKey(gun).getNamespace().equals("cgs")){
   for(Item a:attachments){p.setGameMode(GameType.SURVIVAL);p.getInventory().clearContent();AttachmentContainer m=menu(p,new ItemStack(gun));if(slot(m,new ItemStack(a))!=null){exercise(level,p,gun,a,false);exercise(level,p,gun,a,true);}}
  }
  check(combinations>10,"broad attachment coverage");magazine(level,p);edgeCases(level,p);System.out.println("ATTACHMENT REGRESSION PASS checks="+checks+" combinations="+combinations);
 }catch(Throwable t){System.out.println("ATTACHMENT REGRESSION FAIL");t.printStackTrace();}finally{event.getServer().halt(false);}}
}
