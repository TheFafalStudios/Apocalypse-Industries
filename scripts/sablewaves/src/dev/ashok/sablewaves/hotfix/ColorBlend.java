package dev.ashok.sablewaves.hotfix;
public final class ColorBlend {
    private ColorBlend() {}
    public static int blend(int c00, int c10, int c01, int c11, double x, double z) {
        x = Math.max(0, Math.min(1, x)); z = Math.max(0, Math.min(1, z));
        int color = 0;
        for (int shift = 0; shift <= 16; shift += 8) {
            double a = ((c00 >> shift) & 255) * (1 - x) + ((c10 >> shift) & 255) * x;
            double b = ((c01 >> shift) & 255) * (1 - x) + ((c11 >> shift) & 255) * x;
            color |= ((int)Math.round(a * (1 - z) + b * z)) << shift;
        }
        return color;
    }
}
