package dev.apocalypse.pp;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;
import net.mcreator.protectionpixel.network.ProtectionPixelModVariables;

@Mod(ApocalypsePP.ID)
public final class ApocalypsePP {
    public static final String ID = "apocalypse_pp";
    public static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredItem<Item> ECHO_PLATE = ITEMS.register("echo_plate", EchoPlate::new);
    public ApocalypsePP(IEventBus bus) {
        ITEMS.register(bus);
        NeoForge.EVENT_BUS.addListener(this::tooltip);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::tick);
        NeoForge.EVENT_BUS.addListener(this::commands);
    }
    public static String id(ItemStack s) { return BuiltInRegistries.ITEM.getKey(s.getItem()).toString(); }
    public static boolean eligible(ItemStack s) {
        return s.getCount() == 1 && s.getItem() instanceof ArmorItem && id(s).startsWith("protection_pixel:");
    }
    public static boolean plated(ItemStack s) { return EchoPlate.intactIn(s); }
    public static int count(Player player) {
        int n = 0;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack s = player.getItemBySlot(slot);
            if (plated(s) && ((ArmorItem)s.getItem()).getEquipmentSlot() == slot) n++;
        }
        return n;
    }
    // Validate actual contents rather than trusting PP's cached active flag after removal.
    public static boolean reactorValid(Player player) {
        var v = player.getData(ProtectionPixelModVariables.PLAYER_VARIABLES);
        if (!id(v.powerslot).equals("protection_pixel:powerengine")) return false;
        var c = v.powerslot.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if (c.getSlots() < 2 || c.getSlots() > 9) return false;
        ItemStack water = c.getStackInSlot(0);
        if (!id(water).equals("protection_pixel:watertank") || water.getCount() != 1 || water.getDamageValue() >= 2880) return false;
        boolean fuel = false;
        for (int i = 1; i < c.getSlots(); i++) {
            ItemStack rod = c.getStackInSlot(i);
            if (rod.isEmpty()) continue;
            if (!id(rod).equals("protection_pixel:flarerod") || rod.getCount() != 1 || rod.getDamageValue() >= rod.getMaxDamage()) return false;
            fuel = true;
        }
        return fuel;
    }
    public static boolean powered(Player p) {
        return reactorValid(p) && p.getData(ProtectionPixelModVariables.PLAYER_VARIABLES).active;
    }
    public static void sanitize(Player p) {
        var v = p.getData(ProtectionPixelModVariables.PLAYER_VARIABLES);
        if (!reactorValid(p) && v.active) { v.active = false; v.markSyncDirty(); }
        var exo = v.steamexo.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!powered(p) || !id(v.steamexo).equals("protection_pixel:steamectoskeleton") || !exo.getBoolean("chest")) {
            p.getPersistentData().putDouble("ppexoattack", 0);
            p.getPersistentData().putDouble("ppbreakspeed", 0);
        }
        LancerSafety.clamp(p.getItemBySlot(EquipmentSlot.HEAD));
    }
    public static void applyResonance(Player p) {
        if (count(p) == 4 && powered(p)) {
            if (p instanceof net.minecraft.server.level.ServerPlayer sp && !(p instanceof net.neoforged.neoforge.common.util.FakePlayer)) {
                var a = sp.server.getAdvancements().get(net.minecraft.resources.ResourceLocation.parse("apocalypse_pp:resonance"));
                if (a != null && !sp.getAdvancements().getOrStartProgress(a).isDone()) sp.getAdvancements().award(a, "powered_full_set");
            }
            // Do not remove or replace stronger/longer effects from potions, Echo, or other mods.
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 1, false, false));
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 1, false, false));
        }
    }
    public static boolean workingLoadout(Player p) {
        if (!powered(p)) return false;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack armor = p.getItemBySlot(slot);
            if (eligible(armor) && ((ArmorItem)armor.getItem()).getEquipmentSlot() == slot) return true;
        }
        return false;
    }
    public static void awardWorkingLoadout(Player p) {
        if (workingLoadout(p) && p instanceof net.minecraft.server.level.ServerPlayer sp
                && !(p instanceof net.neoforged.neoforge.common.util.FakePlayer)) {
            var a = sp.server.getAdvancements().get(net.minecraft.resources.ResourceLocation.parse("apocalypse_pp:powered_loadout"));
            if (a != null && !sp.getAdvancements().getOrStartProgress(a).isDone()) sp.getAdvancements().award(a, "wear_powered_armor");
        }
    }
    private void tick(PlayerTickEvent.Post e) {
        Player p = e.getEntity();
        if (p.level().isClientSide()) return;
        sanitize(p);
        EchoPlate.prepareMenu(p);
        LancerSafety.forgetIfUnequipped(p);
        if (p.tickCount % 10 == 0) { applyResonance(p); awardWorkingLoadout(p); }
    }
    private void tooltip(ItemTooltipEvent e) {
        if (e.getItemStack().is(ECHO_PLATE.get())) {
            e.getToolTip().add(Component.literal("Intact Echo Plate in each armor piece + powered reactor: Resistance II, Strength II."));
            e.getToolTip().add(Component.literal("Fit and recover using the Armor Load Platform plate menu."));
            e.getToolTip().add(Component.literal("Alloy plate stats and durability. Repair worn plates with the plate repair kit."));
        } else if (plated(e.getItemStack())) e.getToolTip().add(Component.literal("Intact Echo Plate installed (one of four required armor pieces)"));
    }
    private void commands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("ppresonance").executes(c -> {
            Player p = c.getSource().getPlayerOrException();
            c.getSource().sendSuccess(() -> Component.literal("Echo Resonance: " + count(p) + "/4; reactor " + (powered(p) ? "powered" : "offline")), false); return count(p);
        }).then(Commands.literal("selftest").requires(s -> s.hasPermission(2)).executes(c -> SelfTest.run(c.getSource()))));
    }
}
