package dev.apocalypse.mace;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
@Mod(value="apocalypse_mace_visuals",dist=Dist.CLIENT)
public final class MaceVisuals {
 private static final org.slf4j.Logger LOGGER=com.mojang.logging.LogUtils.getLogger();
 private final java.util.List<net.minecraft.client.renderer.texture.TextureAtlasSprite> windSprites=new java.util.ArrayList<>();
 private Object spriteUtil;
 private java.lang.reflect.Method markActive;
 private boolean activationFailed;
 public MaceVisuals(IEventBus bus) {
  bus.addListener(this::setup);
  bus.addListener(this::stitched);
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::frame);
 }
 private void stitched(net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent event) {
  if (!event.getAtlas().location().equals(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)) return;
  windSprites.clear();
  for (String path : java.util.List.of("item/maces/improved_wind_animated", "item/maces/improved_wind_third_animated")) {
  var id=ResourceLocation.fromNamespaceAndPath("apocalypse_weapon_library",path);
  var sprite=event.getAtlas().getSprite(id);
  long frames=sprite.contents().getUniqueFrames().count();
  LOGGER.info("[Mace wind animation] sprite={} loaded={} frameSize={}x{} frames={}",id,sprite.contents().name(),sprite.contents().width(),sprite.contents().height(),frames);
  if (sprite.contents().name().equals(id) && frames>1) windSprites.add(sprite);
  }
 }
 private void frame(net.neoforged.neoforge.client.event.RenderFrameEvent.Pre event) {
  if (windSprites.isEmpty() || activationFailed) return;
  try {
   if (markActive==null) {
    Class<?> api=Class.forName("net.caffeinemc.mods.sodium.api.texture.SpriteUtil");
    spriteUtil=api.getField("INSTANCE").get(null);
    markActive=api.getMethod("markSpriteActive",net.minecraft.client.renderer.texture.TextureAtlasSprite.class);
    LOGGER.info("[Mace wind animation] keeping the wind sprite active through Sodium's public texture API");
   }
   for (var sprite : windSprites) markActive.invoke(spriteUtil,sprite);
  } catch (ReflectiveOperationException error) {
   activationFailed=true;
   LOGGER.warn("[Mace wind animation] could not activate wind sprite",error);
  }
 }
 private void setup(FMLClientSetupEvent event) {
  event.enqueueWork(() -> ItemProperties.register(Items.MACE,
   ResourceLocation.fromNamespaceAndPath("apocalypse_mace_visuals","enchantment_variant"),
   (stack,level,entity,seed) -> MaceVariant.value(stack)));
 }
}
