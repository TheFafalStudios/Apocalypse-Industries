package dev.apocalypse.submarinelava;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.texture.TextureType;
import org.lwjgl.opengl.*;
public final class LavaSamplers {
    public static void register(ProgramSamplers.Builder builder) {
        if (!builder.hasSampler("aiVortexField") && !builder.hasSampler("CdsLavaHullFar") && !builder.hasSampler("aiWaterHullFar")) return;
        register(builder, ((dev.apocalypse.submarinelava.mixin.SamplerProgramAccess)(Object)builder).ai$program());
    }
    public static void register(ProgramSamplers.Builder builder, int program) {
        if (builder.hasSampler("aiVortexField")) VortexSamplers.register(builder, program);
        final int medium = GL20.glGetUniformLocation(program, "aiHullMedium");
        if (builder.hasSampler("aiWaterHullFar"))
            builder.addDynamicSampler(TextureType.TEXTURE_2D, () -> {
                boolean water = com.maxenonyme.createsubmarine.submarine.client.SubmarineFogHandler.shouldFog();
                if (medium >= 0) GL41.glProgramUniform1i(program, medium, LavaVisuals.active ? 2 : water ? 1 : 0);
                int id = water ? dev.apocalypse.submarinelava.mixin.HullAccess.ai$hullDepth() : 0;
                return id != 0 ? id : dev.apocalypse.submarinelava.mixin.HullAccess.ai$outside().getId();
            }, null, "aiWaterHullFar");
        if (builder.hasSampler("CdsLavaHullFar"))
            builder.addDynamicSampler(TextureType.TEXTURE_2D, LavaTexture::get, null, "CdsLavaHullFar");
    }
}
