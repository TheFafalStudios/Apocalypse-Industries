package dev.apocalypse.windaudio;
/** Pure audio response; Weather2 values are relative units, never labelled m/s. */
public final class WindEnvelope {
    private WindEnvelope() {}
    public static float target(double wind, double onset, double full, double maximum, double exposure) {
        if (!Double.isFinite(wind) || wind <= onset) return 0;
        double t = Math.clamp((wind-onset)/Math.max(0.01,full-onset),0,1);
        return (float)(t*t*(3-2*t)*Math.clamp(maximum,0,1)*Math.clamp(exposure,0,1));
    }
    public static float advance(float current, float target, double riseSeconds, double fallSeconds) {
        double seconds=target>current?riseSeconds:fallSeconds;
        float next=(float)(current+(target-current)*(1-Math.exp(-0.05/Math.max(0.05,seconds))));
        return Math.abs(next-target)<0.0001f?target:next;
    }
}
