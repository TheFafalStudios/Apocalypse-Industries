package dev.apocalypse.spears;

import com.notunanancyowen.spears.Spears;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import create_deep_dark.configuration.ConfigConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(EchoSpears.ID)
public final class EchoSpears {
 public static final String ID="apocalypse_echo_spears";
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(ID);
 public static final DeferredItem<Item> ECHO_SPEAR=ITEMS.register("echo_spear", EchoSpear::new);
 public EchoSpears(IEventBus bus) { ITEMS.register(bus); bus.addListener(this::creative); NeoForge.EVENT_BUS.addListener(this::damage); NeoForge.EVENT_BUS.addListener(this::tooltip); }
 private void creative(BuildCreativeModeTabContentsEvent e) {
  if(BuiltInRegistries.CREATIVE_MODE_TAB.getKey(e.getTab()).equals(ResourceLocation.parse("minecraft:combat"))) e.accept(ECHO_SPEAR.get());
 }
 private static int effectAmplifier() { return Boolean.TRUE.equals(ConfigConfiguration.STRONG_SWORD.get()) ? 1 : 0; }
 private void damage(LivingDamageEvent.Post e) {
  if(e.getEntity().level().isClientSide || e.getNewDamage()<=0) return;
  if(!(e.getSource().getDirectEntity() instanceof LivingEntity attacker) || e.getSource().getEntity()!=attacker) return;
  ItemStack weapon=attacker.isUsingItem() ? attacker.getUseItem() : attacker.getMainHandItem();
  if(!weapon.is(ECHO_SPEAR.get())) return;
  int amplifier=effectAmplifier();
  e.getEntity().addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100,amplifier));
  e.getEntity().addEffect(new MobEffectInstance(MobEffects.DARKNESS,100,amplifier));
 }
 private void tooltip(ItemTooltipEvent e) {
  if(!e.getItemStack().is(ECHO_SPEAR.get())) return;
  int at=Math.min(1,e.getToolTip().size());
  String numeral=effectAmplifier()==1 ? " II" : "";
  e.getToolTip().add(at++,Component.literal("\u00a77On hit, inflicts to target:"));
  e.getToolTip().add(at++,Component.literal("\u00a77- \u00a7cWeakness"+numeral+" \u00a77(5 sec.)"));
  e.getToolTip().add(at,Component.literal("\u00a77- \u00a7cDarkness"+numeral+" \u00a77(5 sec.)"));
 }
 private static Item.Properties properties() {
  Item.Properties p=new Item.Properties();
  // Exact Netherite Spear defaults, except 0.1 fewer attacks per second.
  for(TypedDataComponent<?> c:Spears.NETHERITE_SPEAR.components()) copy(p,c);
  var original=Spears.NETHERITE_SPEAR.components().get(DataComponents.ATTRIBUTE_MODIFIERS);
  var changed=original.modifiers().stream().map(entry -> {
   var modifier=entry.modifier();
   if(entry.attribute().equals(Attributes.ATTACK_SPEED) && modifier.id().equals(Item.BASE_ATTACK_SPEED_ID)) {
    modifier=new AttributeModifier(modifier.id(),modifier.amount()-0.1,modifier.operation());
   }
   return new ItemAttributeModifiers.Entry(entry.attribute(),modifier,entry.slot());
  }).toList();
  return p.component(DataComponents.ATTRIBUTE_MODIFIERS,new ItemAttributeModifiers(changed,original.showInTooltip()));
 }
 private static <T> void copy(Item.Properties p,TypedDataComponent<T> c) {p.component(c.type(),c.value());}
 private static final class EchoSpear extends TieredItem {
  EchoSpear(){super(((TieredItem)Spears.NETHERITE_SPEAR).getTier(),properties());}
  public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
   return Spears.NETHERITE_SPEAR.use(level,player,hand);
  }
  public int getUseDuration(ItemStack stack,LivingEntity entity) {
   return Spears.NETHERITE_SPEAR.getUseDuration(stack,entity);
  }
  public void inventoryTick(ItemStack s,Level l,Entity e,int slot,boolean selected){Spears.NETHERITE_SPEAR.inventoryTick(s,l,e,slot,selected);}
  public boolean canAttackBlock(BlockState s,Level l,BlockPos p,Player player){return Spears.NETHERITE_SPEAR.canAttackBlock(s,l,p,player);}
  public boolean hurtEnemy(ItemStack s,LivingEntity target,LivingEntity attacker){return Spears.NETHERITE_SPEAR.hurtEnemy(s,target,attacker);}
  public void postHurtEnemy(ItemStack s,LivingEntity target,LivingEntity attacker){Spears.NETHERITE_SPEAR.postHurtEnemy(s,target,attacker);}
 }
}
