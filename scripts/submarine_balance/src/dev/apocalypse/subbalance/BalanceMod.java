package dev.apocalypse.subbalance;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import java.util.Set;

@Mod("apocalypse_submarine_balance")
public final class BalanceMod {
    public static final String REINFORCED = "apocalypse_netherite_pressure_jacket";
    public static final Set<String> FITTINGS = Set.of("ballast_tank","ballast_vent","barometer","command_sub",
        "decompression_chamber","electrolyzer","industrial_alarm","oxygene_diffuser","pump_controller",
        "sonar","submarine_propeller","water_thruster");
    public BalanceMod() { NeoForge.EVENT_BUS.addListener(BalanceMod::reinforce); }
    public static boolean eligible(net.minecraft.world.level.block.state.BlockState state) {
        String id=Balance.id(state);
        return id.startsWith("create_submarine:") && FITTINGS.contains(id.substring(id.indexOf(':')+1)) && !Balance.window(state);
    }
    private static void reinforce(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown() || !event.getItemStack().is(Items.NETHERITE_BLOCK)) return;
        var level=event.getLevel(); var pos=event.getPos();
        var be=level.getBlockEntity(pos);
        if (be==null || !eligible(level.getBlockState(pos))) return;
        event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) return;
        if (be.getPersistentData().getBoolean(REINFORCED)) {
            event.getEntity().displayClientMessage(Component.literal("This fitting already has a netherite pressure jacket."),true);
            return;
        }
        be.getPersistentData().putBoolean(REINFORCED,true); be.setChanged();
        if (!event.getEntity().getAbilities().instabuild) event.getItemStack().shrink(1);
        var id=com.maxenonyme.createsubmarine.submarine.util.SubLevelRegistry.findUUID(level,pos);
        if (id!=null) com.maxenonyme.createsubmarine.submarine.system.SubmarinePressureSystem.invalidate(id);
        event.getEntity().displayClientMessage(Component.literal("Netherite pressure jacket installed: water 225, lava 170 before hull bonuses. Breaking this fitting loses the jacket."),false);
    }
}
