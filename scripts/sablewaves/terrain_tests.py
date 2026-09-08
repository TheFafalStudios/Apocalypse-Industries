"""Exercise compiled production helpers against instrumented terrain stand-ins.
This is a headless regression test, not a Minecraft gameplay test.
"""
from pathlib import Path
from build import WORK, SOURCE, OUTPUT, INPUT, run
stubs = {
'net/minecraft/core/BlockPos.java': '''package net.minecraft.core;
public record BlockPos(int x,int y,int z) { public int getX(){return x;} public int getY(){return y;} public int getZ(){return z;} }
''',
'net/minecraft/world/level/material/FluidState.java': '''package net.minecraft.world.level.material;
public record FluidState(boolean water) {}
''',
'net/minecraft/world/level/material/Fluid.java': '''package net.minecraft.world.level.material;
public class Fluid { public FluidState defaultFluidState(){return new FluidState(false);} }
''',
'net/minecraft/world/level/material/EmptyFluid.java': '''package net.minecraft.world.level.material;
public class EmptyFluid extends Fluid {}
''',
'net/minecraft/world/level/material/Fluids.java': '''package net.minecraft.world.level.material;
public class Fluids { public static final Fluid EMPTY=new EmptyFluid(); }
''',
'net/minecraft/world/level/chunk/LevelChunk.java': '''package net.minecraft.world.level.chunk;
import net.minecraft.core.BlockPos; import net.minecraft.world.level.material.FluidState;
public class LevelChunk { public final java.util.function.Predicate<BlockPos> water;
public LevelChunk(java.util.function.Predicate<BlockPos> water){this.water=water;}
public FluidState getFluidState(BlockPos pos){return new FluidState(water.test(pos));} }
''',
'net/minecraft/server/level/ServerChunkCache.java': '''package net.minecraft.server.level;
import java.util.*; import net.minecraft.world.level.chunk.LevelChunk;
public class ServerChunkCache { public final Map<Long,LevelChunk> chunks=new HashMap<>();
public int queries; public static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
public LevelChunk getChunkNow(int x,int z){queries++; return chunks.get(key(x,z));} }
''',
'net/minecraft/world/level/Level.java': '''package net.minecraft.world.level;
import net.minecraft.core.BlockPos; import net.minecraft.world.level.material.FluidState;
public class Level { public boolean hasChunk(int x,int z){return true;}
public FluidState getFluidState(BlockPos pos){return new FluidState(true);} }
''',
'net/minecraft/server/level/ServerLevel.java': '''package net.minecraft.server.level;
import net.minecraft.core.BlockPos; import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk; import net.minecraft.world.level.material.FluidState;
public class ServerLevel extends Level {
public int blockingCalls, missingRequests; public final ServerChunkCache cache=new ServerChunkCache();
public ServerChunkCache getChunkSource(){return cache;}
@Override public FluidState getFluidState(BlockPos pos){blockingCalls++;
LevelChunk c=cache.getChunkNow(pos.getX()>>4,pos.getZ()>>4);
if(c==null){missingRequests++;throw new IllegalStateException("would request/wait for unloaded chunk");}
return c.getFluidState(pos);}
@Override public boolean hasChunk(int x,int z){return cache.getChunkNow(x,z)!=null;}
public void fill(int min,int max,java.util.function.Predicate<BlockPos> water){
for(int x=min;x<=max;x++)for(int z=min;z<=max;z++) cache.chunks.put(ServerChunkCache.key(x,z),new LevelChunk(water));}
}
''',
'dev/ashok/sablewaves/SeaTags.java': '''package dev.ashok.sablewaves;
import net.minecraft.world.level.material.FluidState;
public class SeaTags { public static boolean isSeaWater(FluidState state){return state.water();} }
''',
'dev/ashok/sablewaves/WavesConfig.java': '''package dev.ashok.sablewaves;
public class WavesConfig { public double minFetchFactor=0.25, maxFetchBlocks=320; public boolean fetchEnabled=true; }
''',
}
base=WORK/'tests/stubs'; classes=WORK/'tests/classes'
classes.mkdir(parents=True,exist_ok=True)
paths=[]
for name,text in stubs.items():
    p=base/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text,encoding='utf-8');paths.append(p)
run('javac',['--release','21','-proc:none','-classpath',OUTPUT,'-d',classes]+paths+[SOURCE/'tests/TerrainTests.java'],'terrain-compile')
run('java',['-Xverify:all','-classpath',str(classes)+';'+str(OUTPUT),'TerrainTests'],'terrain-tests')
# Demonstrate that the previous JAR has the two regressions these tests target.
run('java',['-Xverify:all','-classpath',str(classes)+';'+str(INPUT),'TerrainTests','baseline'],'terrain-baseline')

