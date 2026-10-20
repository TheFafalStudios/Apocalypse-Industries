package dev.apocalypse.submarinelava;
import com.maxenonyme.vortexengine.VortexConfig;
import com.maxenonyme.vortexengine.VortexEngine;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
public final class VortexBridge {
    public static boolean enabled = false;
    public static long updates;
    public static boolean active() {
        return enabled && IrisApi.getInstance().isShaderPackInUse()
            && "ApocalypseAtmosphere_NetherDH_v1.2.zip".equals(Iris.getCurrentPackName())
            && VortexEngine.enabled();
    }
    public static boolean lights() { return active() && VortexConfig.LIGHTS.get(); }
    public static boolean mist() { return active() && VortexConfig.MIST.get(); }
}
