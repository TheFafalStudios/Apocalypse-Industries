package dev.apocalypse.envelope;
import java.util.Map;
import java.util.WeakHashMap;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import weather2.ServerTickHandler;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.tornado.simple.TornadoFunnelSimple;
public final class DamagePolicy {
 private static final TagKey<Block> ENVELOPE=TagKey.create(Registries.BLOCK,ResourceLocation.fromNamespaceAndPath("aeronautics","envelope"));
 // Server-thread only. Weak keys do not retain removed/unloaded ships.
 private static final Map<ServerSubLevel,DamageMath.Gate> GATES=new WeakHashMap<>();
 public static boolean isEnvelope(BlockState state) {return state.is(ENVELOPE);}
 public static boolean tryPuncture(ServerSubLevel ship,ServerLevel world,BlockPos localPos) {
  var embedded=ship.getPlot().getEmbeddedLevelAccessor().getLevel();
  if(!isEnvelope(embedded.getBlockState(localPos)))return false;
  boolean exposed=false;
  for(var side:Direction.values())if(embedded.getBlockState(localPos.relative(side)).isAir()){exposed=true;break;}
  if(!exposed)return false;
  var gate=GATES.computeIfAbsent(ship,ignored->new DamageMath.Gate());long tick=world.getGameTime();
  if(!gate.ready(tick))return false;
  Vec3 position=ship.logicalPose().transformPosition(Vec3.atCenterOf(localPos));
  int terrain=surface(world,position.x,position.z);
  if(terrain==Integer.MAX_VALUE||position.y-terrain<Settings.ENVELOPE_CLEARANCE.get())return false;
  var bounds=ship.boundingBox();
  double cx=(bounds.minX()+bounds.maxX())*.5,cz=(bounds.minZ()+bounds.maxZ())*.5;
  double[][] samples={{cx,cz},{bounds.minX(),bounds.minZ()},{bounds.minX(),bounds.maxZ()},{bounds.maxX(),bounds.minZ()},{bounds.maxX(),bounds.maxZ()}};
  for(var sample:samples) {
   int ground=surface(world,sample[0],sample[1]);
   if(ground==Integer.MAX_VALUE||bounds.minY()-ground<Settings.HULL_CLEARANCE.get())return false;
  }
  var manager=ServerTickHandler.getWeatherManagerFor(world.dimension());if(manager==null)return false;
  double chance=0;
  for(var object:manager.getStormObjects())if(object instanceof StormObject storm&&!storm.isDead&&storm.levelCurIntensityStage>StormObject.STATE_FORMING)
   chance=Math.max(chance,funnelChance(position,storm.getTornadoFunnelSimple()));
  if(chance<=0||!gate.begin(tick,Settings.INTERVAL.get()))return false;
  if(world.random.nextDouble()>=chance)return false;
  // Always one block, including upstream SABLE cluster mode. Normal removal handles containment updates.
  if(!embedded.destroyBlock(localPos,net.killey.tornadophysics.Config.DROP_BLOCKS.get()))return false;
  gate.success(tick,Settings.COOLDOWN.get());return true;
 }
 private static int surface(ServerLevel world,double x,double z) {
  if(!Double.isFinite(x)||!Double.isFinite(z))return Integer.MAX_VALUE;
  int bx=(int)Math.floor(x),bz=(int)Math.floor(z);
  if(!world.hasChunk(bx>>4,bz>>4))return Integer.MAX_VALUE;
  return world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,bx,bz);
 }
 public static double funnelChance(Vec3 point,TornadoFunnelSimple funnel) {
  if(funnel==null||funnel.getConfig()==null||funnel.listLayers==null||funnel.listLayers.isEmpty())return 0;
  var layers=funnel.listLayers;var config=funnel.getConfig();double margin=Settings.MARGIN.get(),chance=0;
  // Interpolate real server layer centers at this envelope's Y, not the cloud center.
  for(int i=0;i<layers.size();i++) {
   Vec3 a=layers.get(i).getPos(),b=i+1<layers.size()?layers.get(i+1).getPos():a;
   if(a==null||b==null)continue;
   double low=Math.min(a.y,b.y),high=Math.max(a.y,b.y),tolerance=(i==0||i==layers.size()-1)?margin:0;
   if(point.y<low-tolerance||point.y>high+tolerance)continue;
   double t=Math.abs(b.y-a.y)<1e-9?0:Math.clamp((point.y-a.y)/(b.y-a.y),0,1);
   double dx=point.x-(a.x+(b.x-a.x)*t),dz=point.z-(a.z+(b.z-a.z)*t);
   double radius=Math.min(Settings.MAX_RADIUS.get(),config.getRadiusOfBase()+config.getRadiusIncreasePerLayer()*(i+t)+margin);
   chance=Math.max(chance,DamageMath.probability(Math.hypot(dx,dz),radius,Settings.CHANCE.get()));
  }
  return chance;
 }
 private DamagePolicy() {}
}
