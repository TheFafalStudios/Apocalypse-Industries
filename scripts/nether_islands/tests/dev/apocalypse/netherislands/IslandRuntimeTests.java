package dev.apocalypse.netherislands;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("apocalypse_island_tests")
public final class IslandRuntimeTests {
    public IslandRuntimeTests() { NeoForge.EVENT_BUS.addListener(this::run); }

    private void run(ServerStartedEvent event) {
        var server = event.getServer();
        var report = new LinkedHashMap<String, Object>();
        try {
            var level = server.getLevel(Level.NETHER);
            boolean enabled = IslandFoundations.enabled(level);
            report.put("foundationsEnabled", enabled);
            if(IslandFoundations.enabled(server.overworld())) throw new AssertionError("Overworld scope leaked");
            if(Boolean.getBoolean("apocalypse.islands.allStructures")) {
                NetherStructureAudit.run(level,report);
                report.put("pass",true);
                return;
            }
            if(Boolean.getBoolean("apocalypse.islands.portalsOnly")) {
                PortalAudit.run(level,server.overworld(),report);
                report.put("pass",true);
                return;
            }
            if(Boolean.getBoolean("apocalypse.islands.ecologyOnly")) {
                EcologyAudit.run(level,report);
                report.put("pass",true);
                return;
            }
            if(Boolean.getBoolean("apocalypse.islands.elevatedOnly")) {
                if(level.getChunkSource().getGenerator().getSeaLevel()!=100) throw new AssertionError("Wrong sea level");
                LayerAudit.run(level,report);
                DebrisAudit.run(level,report);
                SeabedAudit.run(level,report);
            }
            if(Boolean.getBoolean("apocalypse.islands.seabedOnly")) {
                SeabedAudit.run(level,report);
                report.put("pass",true);
                return;
            }
            if(Boolean.getBoolean("apocalypse.islands.layersOnly")) {
                LayerAudit.run(level,report);
                report.put("pass",true);
                return;
            }
            if(Boolean.getBoolean("apocalypse.islands.debrisOnly")) {
                DebrisAudit.run(level,report);
                report.put("pass",true);
                return;
            }
            if(Boolean.getBoolean("apocalypse.islands.floorOnly")) {
                int checked=0, aboveBedrock=0, roofBedrock=0;
                int bottom=level.getMinBuildHeight();
                for(int[] p:new int[][]{{0,0},{1,0},{-1,-1},{64,32},{-64,-32},{12,-18},{-19,25},{128,-128},{-128,128}}) {
                    var chunk=level.getChunk(p[0],p[1],ChunkStatus.FEATURES);
                    for(int z=0;z<16;z++) for(int x=0;x<16;x++) {
                        int wx=p[0]*16+x,wz=p[1]*16+z;
                        if(!chunk.getBlockState(new BlockPos(wx,bottom,wz)).is(Blocks.BEDROCK))
                            throw new AssertionError("Missing bedrock at " + wx + "," + bottom + "," + wz);
                        checked++;
                        if(chunk.getBlockState(new BlockPos(wx,bottom+1,wz)).is(Blocks.BEDROCK)) aboveBedrock++;
                        if(chunk.getBlockState(new BlockPos(wx,level.getMaxBuildHeight()-1,wz)).is(Blocks.BEDROCK)) roofBedrock++;
                    }
                }
                report.put("bottomY",bottom);
                report.put("bedrockFloorBlocksChecked",checked);
                report.put("bedrockAboveFloor",aboveBedrock);
                report.put("bedrockAtRoof",roofBedrock);
                if(aboveBedrock!=0 || roofBedrock!=0) throw new AssertionError("Extra bedrock layer");
                report.put("pass",true);
                return;
            }
            var random = level.getChunkSource().randomState();
            var generator = level.getChunkSource().getGenerator();
            if(!Boolean.getBoolean("apocalypse.islands.structuresOnly")) {
            int min = level.getMinBuildHeight(), height = level.getHeight();
            int centerX=0, centerZ=0;
            double best=Double.NEGATIVE_INFINITY;
            for(int z=-2048; z<=2048; z+=32) for(int x=-2048; x<=2048; x+=32) {
                double density=random.router().finalDensity().compute(
                    new net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext(x,48,z));
                if(density>best){best=density;centerX=x;centerZ=z;}
            }
            report.put("mapCenter",new int[]{centerX,centerZ});
            report.put("maximumRawDensityAt48",best);
            // Base columns don't generate or modify chunks; record a reproducible
            // 128x128 height grid for comparing density implementations.
            int[][] map = new int[128][128];
            int[] edges = new int[16];
            int land = 0;
            for (int z=0; z<128; z++) {
                for (int x=0; x<128; x++) {
                    var column = generator.getBaseColumn(centerX + x - 64, centerZ + z - 64, level, random);
                    int top = min - 1;
                    for (int y=min+height-1; y>=min; y--) {
                        if (column.getBlock(y).isSolid()) { top=y; break; }
                    }
                    map[z][x] = top;
                    if (top>=32) land++;
                    if (x>0 && Math.abs(top-map[z][x-1])>=8)
                        edges[Math.floorMod(centerX+x-64, 16)]++;
                    if (z>0 && Math.abs(top-map[z-1][x])>=8)
                        edges[Math.floorMod(centerZ+z-64, 16)]++;
                }
            }
            report.put("baseHeights", map);
            report.put("baseLandColumns", land);
            report.put("largeHeightStepsByMod16", edges);
            System.out.println("ISLAND AUDIT base columns complete");
            int[][] actual=new int[128][128];
            var sampled=new java.util.HashMap<Long,net.minecraft.world.level.chunk.ChunkAccess>();
            for(int z=0;z<128;z++) for(int x=0;x<128;x++) {
                int wx=centerX+x-64,wz=centerZ+z-64;
                long key=net.minecraft.world.level.ChunkPos.asLong(wx>>4,wz>>4);
                var chunk=sampled.computeIfAbsent(key,k->level.getChunk(wx>>4,wz>>4,ChunkStatus.NOISE));
                int top=min-1;
                var cursor=new BlockPos.MutableBlockPos();
                for(int y=min+height-1;y>=min;y--) {
                    if(chunk.getBlockState(cursor.set(wx,y,wz)).isSolid()){top=y;break;}
                }
                actual[z][x]=top;
            }
            report.put("actualHeights",actual);
            System.out.println("ISLAND AUDIT actual noise chunks complete");
            }

            var structures = new LinkedHashMap<String, Object>();
            report.put("structures", structures);
            var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            for (String id : new String[]{"minecraft:fortress", "minecraft:bastion_remnant"}) {
                var structure = registry.get(ResourceLocation.parse(id));
                var result = new LinkedHashMap<String, Object>();
                structures.put(id,result);
                // Vanilla /locate's structure search, restricted to a single ID.
                var holder = registry.wrapAsHolder(structure);
                var found = generator.findNearestMapStructure(level,
                    net.minecraft.core.HolderSet.direct(holder), BlockPos.ZERO, 64, false);
                if (found == null) throw new AssertionError("No structure found: " + id);
                var position = found.getFirst();
                result.put("position", new int[]{position.getX(), position.getY(), position.getZ()});
                var origin = level.getChunk(position.getX()>>4, position.getZ()>>4, ChunkStatus.STRUCTURE_STARTS);
                StructureStart start = origin.getStartForStructure(structure);
                if (start == null || !start.isValid()) throw new AssertionError("Missing start: " + id);
                result.put("pieces", start.getPieces().size());
                int checked=0, supported=0;
                var perPiece = new java.util.ArrayList<Object>();
                // Full real generation of representative pieces, including the
                // structure itself; inspect the terrain immediately below floors.
                for (int i=0; i<start.getPieces().size(); i++) {
                    var piece=start.getPieces().get(i);
                    var b=piece.getBoundingBox();
                    int ground=b.minY();
                    if (piece instanceof net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece p)
                        ground += p.getGroundLevelDelta();
                    int floor=ground-1;
                    for(int[] point:new int[][]{{(b.minX()+b.maxX())/2,(b.minZ()+b.maxZ())/2},
                        {b.minX(),b.minZ()},{b.minX(),b.maxZ()},{b.maxX(),b.minZ()},{b.maxX(),b.maxZ()}}) {
                        int x=point[0],z=point[1];
                        var chunk=level.getChunk(x>>4,z>>4,ChunkStatus.NOISE);
                        boolean solid=chunk.getBlockState(new BlockPos(x,floor,z)).isSolid();
                        if(checked==0) {
                            result.put("firstBlock",chunk.getBlockState(new BlockPos(x,floor,z)).toString());
                            result.put("references",chunk.getAllReferences().toString());
                            var noise=chunk.getOrCreateNoiseChunk(c->null);
                            var beardField=noise.getClass().getDeclaredField("beardifier");
                            beardField.setAccessible(true);
                            var beard=beardField.get(noise);
                            result.put("beardifier",beard.getClass().getName());
                            for(var f:beard.getClass().getDeclaredFields()) if(f.getName().contains("footprints")) {
                                f.setAccessible(true);
                                result.put("footprintCount",((java.util.List<?>)f.get(beard)).size());
                            }
                        }
                        checked++;
                        if(solid) supported++;
                        perPiece.add(new int[]{x,floor,z,solid?1:0});
                    }
                }
                result.put("checkedFloorPoints",checked);
                result.put("supportedFloorPoints",supported);
                result.put("samples",perPiece);
                if(id.equals("minecraft:bastion_remnant")) BastionIslandAudit.check(level,start,result);
                structures.put(id,result);
                System.out.println("ISLAND AUDIT " + id + " supports " + supported + "/" + checked);
                if(enabled && supported!=checked) throw new AssertionError("Unsupported piece in " + id);
            }
            report.put("structures", structures);
            report.put("pass",true);
        } catch(Throwable error) {
            error.printStackTrace();
            report.put("pass",false);
            report.put("error",error.toString());
        } finally {
            try {
                Files.writeString(Path.of(System.getProperty("apocalypse.islands.report", "island-audit.json")),
                    new GsonBuilder().setPrettyPrinting().create().toJson(report));
            } catch(Exception error) { error.printStackTrace(); }
            System.out.println("ISLAND AUDIT COMPLETE pass=" + report.get("pass"));
            server.halt(false);
        }
    }
}
