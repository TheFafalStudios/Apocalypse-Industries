package net.obf.weather2compat.hotfix;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import weather2.weathersystem.storm.StormObject;
/** Stable, typed snapshot for Weather2Compat's rain and sky checks. */
public final class Weather2CompatSafety {
    private Weather2CompatSafety() {}
    // Preserve the original erased (List)Iterator binary signature.
    public static Iterator<StormObject> safeIterator(List<?> storms) {
        List<StormObject> snapshot = new ArrayList<>();
        for (Object storm : new ArrayList<>(storms)) {
            if (storm instanceof StormObject rainStorm) snapshot.add(rainStorm);
        }
        return snapshot.iterator();
    }
}
