package dev.apocalypse.axiomcontrol;

import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import java.util.concurrent.atomic.AtomicLong;

/** Runtime opt-in only: no persisted enabled state can survive a restart. */
@Mod(value="apocalypse_axiom_control", dist=Dist.DEDICATED_SERVER)
public final class AxiomControl {
    private static volatile MinecraftServer owner;
    private static volatile boolean enabled;
    private static final AtomicLong blocked = new AtomicLong();
    public AxiomControl() {
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent event) -> {
            owner=event.getServer(); enabled=false;
        });
        NeoForge.EVENT_BUS.addListener(this::commands);
    }
    public static boolean allowed(MinecraftServer server) {
        return !server.isDedicatedServer() || (owner==server && enabled);
    }
    public static void recordBlocked(){blocked.incrementAndGet();}
    public static long blockedPayloads(){return blocked.get();}
    private void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("axiomserver")
            .requires(source -> source.getServer().isDedicatedServer() && source.hasPermission(4))
            .then(Commands.literal("status").executes(context -> {
                context.getSource().sendSuccess(() -> Component.literal("Axiom is " +
                    (allowed(context.getSource().getServer()) ? "enabled" : "disabled") +
                    ". It starts disabled after every server restart."), false); return 1;
            }))
            .then(Commands.literal("enable").executes(context -> set(context.getSource().getServer(), true, context.getSource())))
            .then(Commands.literal("disable").executes(context -> set(context.getSource().getServer(), false, context.getSource()))));
    }
    private static int set(MinecraftServer server, boolean state, net.minecraft.commands.CommandSourceStack source) {
        owner=server; enabled=state;
        // Native tick will update authorization and the client UI on its next tick.
        source.sendSuccess(() -> Component.literal(state ?
            "Axiom enabled for this server session. Native permissions and licensing still apply." :
            "Axiom disabled. All Axiom requests are blocked immediately."), true);
        return 1;
    }
}
