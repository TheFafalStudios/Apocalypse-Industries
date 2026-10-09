package apocalypse.verify;
import java.util.*;
import java.lang.reflect.*;
import dev.apocalypse.envelope.*;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import weather2.ServerTickHandler;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.tornado.ActiveTornadoConfig;
import weather2.weathersystem.tornado.simple.*;
@Mod("mod_startup_check")
public class ModStartupCheck {
 int assertions;
 public ModStartupCheck(){NeoForge.EVENT_BUS.addListener(this::check);}
 void ok(boolean value,String message){assertions++;if(!value)throw new AssertionError(message);}
 void clear() throws Exception {
  var f=DamagePolicy.class.getDeclaredField("GATES");f.setAccessible(true);((Map<?,?>)f.get(null)).clear();
 }
 ServerSubLevel ship(ServerLevel w,Block block,Vec3 target) {
  var positions=new ArrayList<BlockPos>();
  for(int x=0;x<3;x++)for(int y=90;y<93;y++)for(int z=0;z<3;z++){
   var p=new BlockPos(x,y,z);w.setBlock(p,block.defaultBlockState(),3);positions.add(p);
  }
  var s=SubLevelAssemblyHelper.assembleBlocks(w,positions.getFirst(),positions,BoundingBox3i.from(positions));
  move(s,target);
  return s;
 }
 BlockPos candidate(ServerSubLevel s){
  var b=s.getPlot().getBoundingBox();return new BlockPos(b.minX(),b.minY(),b.minZ());
 }
 void move(ServerSubLevel s,Vec3 target){
  Vec3 old=s.logicalPose().transformPosition(Vec3.atCenterOf(candidate(s)));
  s.logicalPose().position().add(target.x-old.x,target.y-old.y,target.z-old.z);
  s.forceUpdateGlobalBounds();
 }
 int count(ServerSubLevel s){
  var b=s.getPlot().getBoundingBox();var e=s.getPlot().getEmbeddedLevelAccessor().getLevel();int n=0;
  for(var p:BlockPos.betweenClosed(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()))if(!e.getBlockState(p).isAir())n++;
  return n;
 }
 void check(ServerStartedEvent event){
  try{
   var w=event.getServer().overworld();
   for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)w.getChunk(x,z);
   var type=Class.forName("net.killey.tornadophysics.event.TornadoEvent");
   ok(Arrays.stream(type.getDeclaredMethods()).anyMatch(m->m.getName().contains("envelopeTag")),"tag mixin missing");
   ok(Arrays.stream(type.getDeclaredMethods()).anyMatch(m->m.getName().contains("envelopeId")),"ID mixin missing");
   Method destruction=type.getDeclaredMethod("processBlockDestruction",ServerSubLevel.class,ServerLevel.class);destruction.setAccessible(true);
   Object handler=type.getConstructor().newInstance();
   var envelope=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("aeronautics:white_envelope"));
   ok(envelope!=Blocks.AIR&&DamagePolicy.isEnvelope(envelope.defaultBlockState()),"actual Aeronautics envelope tag");
   var manager=ServerTickHandler.getWeatherManagerFor(w.dimension());ok(manager!=null,"live Weather2 manager");
   manager.clearAllStorms();
   var storm=new StormObject(manager);storm.initFirstTime();storm.levelCurIntensityStage=StormObject.STATE_STAGE1;storm.size=300;
   storm.pos=new Vec3(20,200,20);storm.posGround=new Vec3(20,-63,20);
   var funnel=new TornadoFunnelSimple(new ActiveTornadoConfig().setRadiusOfBase(10).setRadiusIncreasePerLayer(0).setHeight(80),storm);
   funnel.getConfig().setRadiusOfBase(10).setRadiusIncreasePerLayer(0);
   funnel.listLayers.add(new Layer(new Vec3(20,80,20)));funnel.listLayers.add(new Layer(new Vec3(20,120,20)));
   storm.setTornadoFunnelSimple(funnel);manager.addStormObject(storm);
   ok(Math.abs(DamagePolicy.funnelChance(new Vec3(20,100,20),funnel)-.1)<1e-9,"default center chance");
   ok(Math.abs(DamagePolicy.funnelChance(new Vec3(29,100,20),funnel)-.025)<1e-9,"quadratic falloff");
   ok(DamagePolicy.funnelChance(new Vec3(38,100,20),funnel)==0,"radius boundary");
   ok(DamagePolicy.funnelChance(new Vec3(20,170,20),funnel)==0,"vertical separation");
   ok(DamagePolicy.funnelChance(new Vec3(20,100,20),null)==0,"missing funnel");
   funnel.getConfig().setRadiusOfBase(100);
   ok(DamagePolicy.funnelChance(new Vec3(53,100,20),funnel)==0,"wide upper funnel radius cap");
   funnel.getConfig().setRadiusOfBase(10);
   var gate=new DamageMath.Gate();
   ok(gate.begin(0,100),"first opportunity");
   for(int t=0;t<100;t++)ok(!gate.begin(t,100),"overlap cannot bypass opportunity "+t);
   ok(gate.begin(100,100),"next opportunity");
   gate.success(100,600);
   for(int t=101;t<700;t++)ok(!gate.begin(t,100),"success cooldown "+t);
   ok(gate.begin(700,100),"cooldown expiry");
   ok(gate.begin(0,100),"clock reset");
   for(int d=0;d<=128;d++)ok(DamageMath.probability(d,32,.1)>=0&&DamageMath.probability(d,32,.1)<=.1,"bounded chance "+d);
   Settings.CHANCE.set(1.0);
   var s=ship(w,envelope,new Vec3(20,100,20));int before=count(s);ok(before==27,"assembled envelope cube");
   clear();ok(DamagePolicy.tryPuncture(s,w,candidate(s)),"close airborne puncture");
   ok(count(s)==26,"exactly one block punctured");
   for(int i=0;i<20;i++)ok(!DamagePolicy.tryPuncture(s,w,candidate(s).offset(1,0,0)),"success shared cooldown");
   // The next storm is identical: it must not multiply opportunity or puncture rates.
   var second=new StormObject(manager);second.initFirstTime();second.levelCurIntensityStage=StormObject.STATE_STAGE1;second.size=300;
   second.pos=storm.pos;
   var secondFunnel=new TornadoFunnelSimple(new ActiveTornadoConfig(),second);
   secondFunnel.getConfig().setRadiusOfBase(10).setRadiusIncreasePerLayer(0);
   secondFunnel.listLayers.add(new Layer(new Vec3(20,80,20)));secondFunnel.listLayers.add(new Layer(new Vec3(20,120,20)));
   second.setTornadoFunnelSimple(secondFunnel);manager.addStormObject(second);
   ok(!DamagePolicy.tryPuncture(s,w,candidate(s).offset(1,0,0)),"overlapping storm cooldown");
   manager.removeWeatherObjectAndSync(second);
   s=ship(w,envelope,new Vec3(80,100,20));clear();before=count(s);
   for(int i=0;i<20;i++)destruction.invoke(handler,s,w);
   ok(count(s)==before,"old wide damage radius protected");
   s=ship(w,envelope,new Vec3(20,-60,20));clear();before=count(s);
   for(int i=0;i<20;i++)destruction.invoke(handler,s,w);
   ok(count(s)==before,"parked envelope protected");
   s=ship(w,envelope,new Vec3(20,100,20));clear();before=count(s);
   // Raised ground under the lower hull, without changing envelope altitude.
   var highBounds=s.boundingBox();
   var highGround=new BlockPos((int)Math.floor((highBounds.minX()+highBounds.maxX())*.5),99,(int)Math.floor((highBounds.minZ()+highBounds.maxZ())*.5));
   w.setBlock(highGround,Blocks.STONE.defaultBlockState(),3);
   ok(!DamagePolicy.tryPuncture(s,w,candidate(s)),"parked on high terrain protected");
   w.setBlock(highGround,Blocks.AIR.defaultBlockState(),3);
   storm.levelCurIntensityStage=StormObject.STATE_FORMING;clear();
   ok(!DamagePolicy.tryPuncture(s,w,candidate(s)),"F0/forming excluded");
   storm.levelCurIntensityStage=StormObject.STATE_STAGE1;
   Settings.CHANCE.set(0.0);clear();
   for(int i=0;i<20;i++)destruction.invoke(handler,s,w);
   ok(count(s)==before,"mixin suppresses upstream envelope deletion");
   Settings.CHANCE.set(1.0);clear();w.random.setSeed(0);
   destruction.invoke(handler,s,w);
   for(int attempt=0;attempt<100&&count(s)==before;attempt++){clear();destruction.invoke(handler,s,w);}
   System.out.println("TEST DEBUG count="+count(s)+" before="+before+" storms="+manager.getStormObjects().size()+" cachedTags="+net.killey.tornadophysics.Config.cachedBlockTags);
   ok(count(s)==before-1,"mixin produces a single local puncture");
   for(int i=0;i<20;i++)destruction.invoke(handler,s,w);
   ok(count(s)==before-1,"mixin shared ship cooldown");
   // Non-envelope damage is still upstream, even far from all funnels.
   var wood=ship(w,Blocks.OAK_PLANKS,new Vec3(80,100,20));before=count(wood);
   destruction.invoke(handler,wood,w);ok(count(wood)==before-1,"oak damage unchanged");
   Settings.ENABLED.set(false);s=ship(w,envelope,new Vec3(80,100,20));before=count(s);
   destruction.invoke(handler,s,w);ok(count(s)==before-1,"disable restores upstream");
   Settings.ENABLED.set(true);
   // Explicit block IDs must not bypass the envelope policy.
   net.killey.tornadophysics.Config.DESTROYABLE_BLOCKS.set(List.of("aeronautics:white_envelope","minecraft:oak_planks"));
   net.killey.tornadophysics.Config.cachedBlockTags.clear();
   before=count(s);clear();destruction.invoke(handler,s,w);ok(count(s)==before,"explicit ID protected");
   // Even SABLE mode gets a single puncture, never envelope clusters or crumble userdata.
   net.killey.tornadophysics.Config.DESTRUCTION_MODE.set(net.killey.tornadophysics.Config.DestructionMode.SABLE);
   s=ship(w,envelope,new Vec3(20,100,20));before=count(s);clear();w.random.setSeed(0);
   destruction.invoke(handler,s,w);for(int attempt=0;attempt<100&&count(s)==before;attempt++){clear();destruction.invoke(handler,s,w);}ok(count(s)==before-1,"SABLE single puncture");
   ok(s.getUserDataTag()==null||!s.getUserDataTag().getBoolean("tornadophysics:is_crumble"),"ship not marked crumble");
   System.out.println("MOD STARTUP PASS assertions="+assertions+" actual_envelope=true mixin=true native_ships=true");
  }catch(Throwable t){System.out.println("MOD STARTUP FAIL "+t);t.printStackTrace();}
  finally{event.getServer().halt(false);}
 }
}
