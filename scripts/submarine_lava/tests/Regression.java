package dev.apocalypse.submarinelava;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Method;
public final class Regression {
    private static int checks;
    private static void check(boolean value, String name) {
        if (!value) throw new AssertionError(name);
        checks++;
    }
    public static void main(String[] args) throws Exception {
        String source = Files.readString(Path.of(args[0]));
        // Execute the installed upstream Complementary transformer, not a reimplementation.
        var type = Class.forName("com.maxenonyme.createsubmarine.submarine.compat.iris.ComplementarySupport");
        Method patch = type.getDeclaredMethod("patch", String.class);
        patch.setAccessible(true);
        String water = (String)patch.invoke(null, source);
        check(!water.equals(source), "Installed shader must match upstream Complementary integration");
        // Obtain the exact helper and declaration from the installed upstream class constants.
        String upstreamClass = new String(Files.readAllBytes(Path.of(args[1])), java.nio.charset.StandardCharsets.ISO_8859_1);
        String helper = "float cdsWaterBehindHull(vec2 cdsUV, float sceneDepth) {\n";
        int start = upstreamClass.indexOf(helper), end = upstreamClass.indexOf("\n}\n\n", start);
        check(start >= 0 && end > start, "Find installed hull helper");
        String upstream = "uniform sampler2D CdsHullFar;\n" + upstreamClass.substring(start, end + 4) + water;
        String lava = ShaderPatch.patch(upstream);
        check(!lava.equals(upstream), "Lava branch added");
        check(lava.contains("uniform sampler2D CdsLavaHullFar;"), "Dedicated lava mask");
        check(lava.contains("if (aiHullMedium != 1) return -1.0;"), "Water fog requires actual water cabin");
        check(lava.contains("if (aiHullMedium != 2) return -1.0;"), "Lava fog requires actual lava cabin");
        check(lava.contains("uniform sampler2D aiWaterHullFar;") && !lava.contains("CdsHullFar"), "Water mask managed by Iris");
        check(lava.contains("sceneDepth <= hull"), "Interior depth rejected");
        check(lava.contains("length(sceneView.xyz / sceneView.w) - length(hullView.xyz / hullView.w)"), "Exterior-only distance");
        check(lava.contains("if (isEyeInWater == 0 && cdsWater > 0.0)"), "Upstream water branch retained");
        check(ShaderPatch.patch(lava).equals(lava), "Idempotent");
        check(ShaderPatch.patch(null) == null, "Null-safe");
        check(ShaderPatch.patch(source).equals(source), "Does not alter unsupported/unpatched source");
        check(LavaVisuals.submerged(10, 20, h -> true, h -> 2), "Direct exterior lava");
        check(LavaVisuals.submerged(10, 20, h -> h < 14, h -> h >= 14 ? 2 : 0), "Lava above sealed cabin");
        check(!LavaVisuals.submerged(10, 20, h -> h < 14, h -> h >= 14 ? 1 : 0), "Water remains water");
        check(!LavaVisuals.submerged(10, 20, h -> h < 14, h -> h >= 15 ? 2 : 0), "Air above cabin stops search");
        check(!LavaVisuals.submerged(10, 20, h -> false, h -> -1), "Unloaded fluid is inactive");
        check(!LavaVisuals.submerged(10, 20, h -> true, h -> 0), "Bounded all-interior scan");
        String fixture = "uniform sampler2D CdsHullFar;\n" + upstreamClass.substring(start, end + 4)
            + "void main() { float cdsWater = cdsWaterBehindHull(texCoord, z1); }\n";
        for (boolean reduced : new boolean[]{false, true}) {
            for (boolean colored : new boolean[]{false, true}) {
                String preprocessed = fixture + (reduced ? "void DoLavaFog() { fog = sqrt(fog) * 0.4; }\n" : "")
                    + (colored ? "vec3 lightFog = vec3(0.0);\n" : "");
                String result = ShaderPatch.patch(preprocessed);
                check(!result.contains("#"), "No preprocessor directives inserted after preprocessing");
                check(result.contains("exp(-sqrt(aiLava) * 0.4)") == reduced, "Reduced fog resolved in Java");
                check(result.contains("lightFog *= 1.0 - aiLavaFog;") == colored, "Optional colored fog resolved in Java");
            }
        }
        Files.writeString(Path.of(args[2]), lava);
        System.out.println(checks + " regression checks passed; transformed shader saved");
    }
}
