package dev.apocalypse.submarinelava.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(targets="net.irisshaders.iris.gl.program.ProgramSamplers$Builder",remap=false)
public interface SamplerProgramAccess { @Accessor("program") int ai$program(); }
