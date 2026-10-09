package dev.apocalypse.envelope;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
@Mod("apocalypse_envelope_damage")
public final class EnvelopeDamage {
 public EnvelopeDamage(IEventBus bus, ModContainer container) {
  container.registerConfig(ModConfig.Type.COMMON, Settings.SPEC, "apocalypse-envelope-damage-common.toml");
 }
}
