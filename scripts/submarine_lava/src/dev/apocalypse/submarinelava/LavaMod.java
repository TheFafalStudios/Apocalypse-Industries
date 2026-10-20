package dev.apocalypse.submarinelava;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.network.chat.Component;
@Mod(value="apocalypse_submarine_lava",dist=Dist.CLIENT)
public final class LavaMod {
 public LavaMod(IEventBus bus){
  bus.addListener(LavaMod::packs);
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(LavaMod::commands);
 }
 private static void commands(net.neoforged.neoforge.client.event.RegisterClientCommandsEvent event){
  event.getDispatcher().register(com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.minecraft.commands.CommandSourceStack>literal("apovortex")
   .then(com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.minecraft.commands.CommandSourceStack>literal("status").executes(c->status()))
   .then(com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.minecraft.commands.CommandSourceStack>literal("disable").executes(c->{VortexBridge.enabled=false;return status();}))
   .then(com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.minecraft.commands.CommandSourceStack>literal("enable").executes(c->{VortexBridge.enabled=true;return status();})));
 }
 private static int status(){
  var player=net.minecraft.client.Minecraft.getInstance().player;
  if(player!=null)player.displayClientMessage(Component.literal("Vortex bridge: enabled="+VortexBridge.enabled+", active="+VortexBridge.active()
   +", updates="+VortexBridge.updates+", field="+com.maxenonyme.vortexengine.light.LightField.ready()+", mist volumes="+com.maxenonyme.vortexengine.mist.MistField.volumes().size()),false);
  return 1;
 }
 private static void packs(AddPackFindersEvent event){
  if(event.getPackType()==PackType.CLIENT_RESOURCES)
   event.addPackFinders(ResourceLocation.fromNamespaceAndPath("apocalypse_submarine_lava","resourcepacks/vortexfix"),
    PackType.CLIENT_RESOURCES,Component.literal("Apocalypse Vortex GLSL Repair"),PackSource.BUILT_IN,true,Pack.Position.TOP);
 }
}
