package dev.apocalypse.submarinelava;
/** Extends the recognized Deep Seas Complementary patch after upstream applies it. */
public final class ShaderPatch {
    public static String patch(String source) {
        if (source == null || source.contains("CdsLavaHullFar")) return source;
        String declaration = "uniform sampler2D CdsHullFar;";
        String anchor = "float cdsWater = cdsWaterBehindHull(texCoord, z1);";
        if (!source.contains(declaration) || !source.contains(anchor)) return source;
        int start = source.indexOf("float cdsWaterBehindHull(");
        int end = source.indexOf("\n}", start);
        if (start < 0 || end < 0) throw new IllegalStateException("Deep Seas hull helper changed");
        String helper = source.substring(start, end + 2)
            .replace("cdsWaterBehindHull", "aiLavaBehindHull").replace("CdsHullFar", "CdsLavaHullFar");
        source = source.substring(0, start) + helper + "\n\n" + source.substring(start);
        source = source.replace(declaration, declaration + "\nuniform sampler2D CdsLavaHullFar;");
        // This hook runs AFTER Iris preprocessing. Resolve feature choices in Java:
        // adding #ifdef/#endif here makes Iris' AST parser reject the entire shader.
        boolean reducedFog = java.util.regex.Pattern.compile(
            "fog\\s*=\\s*sqrt\\s*\\(\\s*fog\\s*\\)\\s*\\*\\s*0\\.4").matcher(source).find();
        boolean coloredFog = java.util.regex.Pattern.compile("\\bvec3\\s+lightFog\\b").matcher(source).find();
        String fogExpression = reducedFog ? "1.0 - exp(-sqrt(aiLava) * 0.4)" : "1.0 - exp(-aiLava * 0.16)";
        String branch = """
            // Apply fog only beyond the far hull face; leave the cabin clear.
            float aiLava = aiLavaBehindHull(texCoord, z1);
            if (isEyeInWater == 0 && aiLava > 0.0) {
                float aiLavaFog = AI_FOG_EXPRESSION;
                if (z1 >= 1.0) aiLavaFog = 1.0;
                // Vanilla lava fog colour in Complementary's pre-gamma colour space.
                color.rgb = mix(color.rgb, vec3(3.0, 0.5, 0.0), aiLavaFog);
                volumetricEffect.rgb *= 1.0 - aiLavaFog;
                AI_COLORED_FOG
            }
            """.replace("AI_FOG_EXPRESSION", fogExpression)
                .replace("AI_COLORED_FOG", coloredFog ? "lightFog *= 1.0 - aiLavaFog;" : "");
        // Gate both exterior effects by the actual cabin medium, independent of sampler contents.
        source = source.replace(anchor, branch + anchor);
        source = source.replace(declaration, declaration + "\nuniform int aiHullMedium;");
        source = source.replace("float cdsWaterBehindHull(vec2 cdsUV, float sceneDepth) {",
            "float cdsWaterBehindHull(vec2 cdsUV, float sceneDepth) {\n    if (aiHullMedium != 1) return -1.0;");
        source = source.replace("float aiLavaBehindHull(vec2 cdsUV, float sceneDepth) {",
            "float aiLavaBehindHull(vec2 cdsUV, float sceneDepth) {\n    if (aiHullMedium != 2) return -1.0;");
        // Iris manages this mask instead of inheriting the native fixed unit's sampler state.
        return source.replace("CdsHullFar", "aiWaterHullFar");
    }
}
