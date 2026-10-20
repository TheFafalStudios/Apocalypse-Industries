package dev.apocalypse.netherislands;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Exact per-block horizontal fields, cached once for each noise chunk. */
public record ColumnCache(DensityFunction argument) implements DensityFunction.SimpleFunction {
    public static final MapCodec<ColumnCache> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(ColumnCache::argument)
    ).apply(i, ColumnCache::new));
    public static final KeyDispatchDataCodec<ColumnCache> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);
    public static final DeferredRegister<MapCodec<? extends DensityFunction>> TYPES =
        DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, "apocalypse_islands");
    static { TYPES.register("cache_columns", () -> MAP_CODEC); }

    @Override public double compute(FunctionContext c) { return argument.compute(c); }
    @Override public double minValue() { return argument.minValue(); }
    @Override public double maxValue() { return argument.maxValue(); }
    @Override public KeyDispatchDataCodec<? extends DensityFunction> codec() { return CODEC; }
    @Override public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ColumnCache(argument.mapAll(visitor)));
    }

    public static final class Resolved implements DensityFunction.SimpleFunction {
        private final ColumnCache source;
        private final int x0, z0, width;
        private final double[] values;
        public Resolved(ColumnCache source, int x0, int z0, int width) {
            this.source=source; this.x0=x0; this.z0=z0; this.width=width;
            values=new double[width*width];
            for(int z=0;z<width;z++) for(int x=0;x<width;x++)
                values[z*width+x]=source.argument.compute(new SinglePointContext(x0+x,0,z0+z));
        }
        @Override public double compute(FunctionContext context) {
            int x=context.blockX()-x0,z=context.blockZ()-z0;
            return x>=0 && x<width && z>=0 && z<width ? values[z*width+x] : source.compute(context);
        }
        @Override public double minValue(){return source.minValue();}
        @Override public double maxValue(){return source.maxValue();}
        @Override public KeyDispatchDataCodec<? extends DensityFunction> codec(){return ColumnCache.CODEC;}
    }
}
