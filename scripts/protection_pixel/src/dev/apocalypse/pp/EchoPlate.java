package dev.apocalypse.pp;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.mcreator.protectionpixel.configuration.PPCONFIGConfiguration;
import net.mcreator.protectionpixel.init.ProtectionPixelModMenus;

/** An ordinary PP plate. Installation, wear allocation and repair remain native. */
public final class EchoPlate extends Item {
    public EchoPlate() {
        super(new Properties().durability(100).fireResistant()
            .component(DataComponents.CUSTOM_DATA, defaults()));
    }
    private static CustomData defaults() {
        CompoundTag n=new CompoundTag();
        n.putDouble("armor",1.5); n.putDouble("toughness",1); n.putDouble("weight",1.5);
        return CustomData.of(n);
    }
    public static void refresh(ItemStack stack) {
        if (!stack.is(ApocalypsePP.ECHO_PLATE.get())) return;
        double armor=PPCONFIGConfiguration.ARMOR3.get();
        double toughness=PPCONFIGConfiguration.TOUGHNESS3.get();
        double weight=PPCONFIGConfiguration.WEIGHT3.get();
        var n=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if(n.getDouble("armor")==armor && n.getDouble("toughness")==toughness && n.getDouble("weight")==weight) return;
        CustomData.update(DataComponents.CUSTOM_DATA,stack,t->{t.putDouble("armor",armor);t.putDouble("toughness",toughness);t.putDouble("weight",weight);});
    }
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected) {
        if(!level.isClientSide()) refresh(stack);
    }
    public static void prepareMenu(Entity entity) {
        if(entity instanceof Player p && p.containerMenu instanceof ProtectionPixelModMenus.MenuAccessor menu)
            menu.getSlots().values().forEach(slot->refresh(slot.getItem()));
    }
    public static int slots(ItemStack armor) {
        if(!ApocalypsePP.eligible(armor))return 0;
        return switch(((ArmorItem)armor.getItem()).getEquipmentSlot()) {
            case HEAD, FEET -> 2;
            case CHEST -> 5;
            case LEGS -> 4;
            default -> 0;
        };
    }
    public static boolean intactIn(ItemStack armor) {
        if(!ApocalypsePP.eligible(armor) || armor.getDamageValue()>=armor.getMaxDamage())return false;
        var n=armor.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        double baseline=n.getDouble("basicdurable");
        if(!Double.isFinite(baseline) || baseline<0)return false;
        double wear=Math.max(0,armor.getDamageValue()-baseline);
        for(int i=1;i<=slots(armor);i++) {
            var id=ResourceLocation.tryParse(n.getString("slot"+i));
            if(id==null)continue;
            var plate=new ItemStack(BuiltInRegistries.ITEM.get(id));
            if(!plate.is(ItemTags.create(ResourceLocation.parse("protection_pixel:plates"))))continue;
            double saved=n.getDouble("s"+i);
            if(!Double.isFinite(saved) || saved<0)return false;
            // Native Fix1..Fix5 saturate at 99/100 and pass excess armor wear to the next plate.
            double capacity=Math.max(0,99-saved);
            if(plate.is(ApocalypsePP.ECHO_PLATE.get()) && capacity>wear)return true;
            wear=Math.max(0,wear-capacity);
        }
        return false;
    }
}
