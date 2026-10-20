package dev.apocalypse.netherislands;

import java.util.List;
import java.util.ArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.fml.common.Mod;

@Mod("apocalypse_nether_islands")
public final class IslandFoundations {
    public IslandFoundations(net.neoforged.bus.api.IEventBus modBus) {
        ColumnCache.TYPES.register(modBus);
        FloatingDebris.TYPES.register(modBus);
    }
    // A dedicated marker ensures ordinary Nether worlds are unaffected.
    public static boolean enabled(LevelAccessor access) {
        ServerLevel level = access instanceof WorldGenRegion r ? r.getLevel()
            : access instanceof ServerLevel s ? s : null;
        if (level == null || level.dimension() != Level.NETHER) return false;
        return level.registryAccess().registryOrThrow(Registries.DENSITY_FUNCTION)
            .containsKey(ResourceLocation.fromNamespaceAndPath("apocalypse_islands", "foundations_enabled"));
    }

    public static final int MARGIN = 16;
    public static final int BASTION_MARGIN = 24;
    public static int seaLevel(LevelAccessor access) {
        ServerLevel level=access instanceof WorldGenRegion r?r.getLevel():access instanceof ServerLevel s?s:null;
        return level==null?32:level.getChunkSource().getGenerator().getSeaLevel();
    }

    public record Footprint(int minX, int minZ, int maxX, int maxZ, int top, int sea, boolean bastion) {
        public boolean affects(int x, int z) {
            int margin=bastion?BASTION_MARGIN:MARGIN;
            return x >= minX - margin && x <= maxX + margin
                && z >= minZ - margin && z <= maxZ + margin;
        }

        // A rounded apron outside each footprint, with a submerged tapered underside.
        // It is world-coordinate based: no random state, chunk clipping or global cache.
        public double contribution(int x, int y, int z) {
            double dx = Math.max(Math.max(minX - x, 0), x - maxX);
            double dz = Math.max(Math.max(minZ - z, 0), z - maxZ);
            double distance = Math.sqrt(dx * dx + dz * dz);
            if(bastion) {
                double variation=2*Math.sin(x*.095+z*.04)+2*Math.sin(z*.13-x*.03);
                if(distance>=BASTION_MARGIN) return 0;
                double rim=smooth((BASTION_MARGIN-distance)*(1+variation/24.0)/16.0);
                double bottom=top-40+16*(1-rim)+2*Math.sin(x*.07+z*.09);
                double lower=smooth((y-bottom)/12.0);
                double upper=Math.max(0,Math.min(1,(top-6*(1-rim)+.75-y)/3));
                return 4*rim*lower*upper;
            }
            double rim = smooth((MARGIN - distance) / (MARGIN - 4.0));
            if (rim == 0) return 0;
            double bottom = top - 24 + 12.0 * (1.0 - rim);
            double lower = smooth((y - bottom) / 12.0);
            double upper = Math.max(0, Math.min(1, (top + 0.75 - y) / 3.0));
            return 4.0 * rim * lower * upper;
        }

        private static double smooth(double x) {
            double t = Math.max(0, Math.min(1, x));
            return t * t * (3 - 2 * t);
        }
    }

    public static List<Footprint> footprints(List<StructureStart> starts, int sea, int chunkX, int chunkZ,
            net.minecraft.world.level.levelgen.structure.Structure bastion) {
        List<Footprint> result = new ArrayList<>();
        for (StructureStart start : starts) {
            if (!start.isValid()) continue;
            // Modded complexes can overwrite terrain with template air just like
            // bastions. Support their whole envelope below every template bound.
            // Fortresses retain their established per-piece bridge supports.
            if(!(start.getStructure() instanceof net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure)) {
                BoundingBox b=BoundingBox.encapsulatingBoxes(start.getPieces().stream()
                    .map(p->p.getBoundingBox()).toList()).orElseThrow();
                if(b.intersects(chunkX-BASTION_MARGIN,chunkZ-BASTION_MARGIN,chunkX+15+BASTION_MARGIN,chunkZ+15+BASTION_MARGIN))
                    result.add(new Footprint(b.minX(),b.minZ(),b.maxX(),b.maxZ(),b.minY()-1,sea,true));
            }
            for (var piece : start.getPieces()) {
                BoundingBox b = piece.getBoundingBox();
                if (!b.intersects(chunkX - MARGIN, chunkZ - MARGIN,
                                  chunkX + 15 + MARGIN, chunkZ + 15 + MARGIN)) continue;
                // Jigsaw ground level is distinct from the piece's lower bounding box.
                int ground = b.minY();
                if (piece instanceof net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece p)
                    ground += p.getGroundLevelDelta();
                result.add(new Footprint(b.minX(), b.minZ(), b.maxX(), b.maxZ(), ground - 1, sea,false));
            }
        }
        return List.copyOf(result);
    }
}
