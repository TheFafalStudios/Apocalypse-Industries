// Live Vortex field and mist, composed before Complementary bloom/tonemapping.
// The addon allocates textures through Iris and supplies camera-relative grid coordinates.
uniform sampler2D aiVortexHull;
uniform sampler3D aiVortexField;
uniform sampler3D aiVortexOcclusion;
uniform vec4 aiVortexGrid; // camera - field origin, propagation mode (0 = unavailable)
uniform vec4 aiVortexTuning; // brightness, haze, pixel lighting, bridge active
uniform int aiMistCount;
uniform vec4 aiMistPosition[16];
uniform vec4 aiMistColor[16];
vec3 AiVortexLight(vec3 point) {
    if (aiVortexGrid.w < 0.5) return vec3(0.0);
    vec3 grid = (point + aiVortexGrid.xyz) / 128.0;
    if (any(lessThanEqual(grid, vec3(0.005))) || any(greaterThanEqual(grid, vec3(0.995)))) return vec3(0.0);
    float openCell = 1.0 - texture3D(aiVortexOcclusion, grid).r;
    if (openCell < 0.02) return vec3(0.0);
    vec4 field = texture3D(aiVortexField, grid) / openCell;
    vec3 hue;
    float level;
    if (aiVortexGrid.w < 1.5) {
        if (field.a <= 0.0001) return vec3(0.0);
        hue = field.rgb / field.a;
        level = pow(clamp(log(field.a / 0.0001) / 10.2, 0.0, 1.0), 1.5);
    } else {
        hue = field.rgb;
        level = floor(field.a * 15.0 + 0.5) / 15.0 * 0.8;
    }
    hue /= max(max(hue.r, hue.g), max(hue.b, 0.001));
    vec3 result = pow(max(hue, vec3(0.0)), vec3(1.5)) * level;
    // Fade at the field edges instead of drawing a moving cube around the camera.
    vec3 edge = min(grid, 1.0 - grid) * 128.0;
    return result * smoothstep(0.0, 6.0, min(edge.x, min(edge.y, edge.z)));
}
float AiVortexNoise(vec3 p) {
    return 0.5 + 0.25 * sin(p.x * 0.31 + sin(p.z * 0.43) + frameTimeCounter * 0.33)
               + 0.25 * sin(p.y * 0.47 - p.z * 0.19 - frameTimeCounter * 0.21);
}
void AiApplyVortex(inout vec3 color, vec2 uv, float frontDepth, float backDepth) {
    if (aiVortexTuning.w < 0.5 || frontDepth <= 0.56) return;
    vec4 aiView = gbufferProjectionInverse * vec4(vec3(uv, backDepth) * 2.0 - 1.0, 1.0);
    aiView /= aiView.w;
    bool aiSurface = backDepth < 1.0;
    #ifdef DISTANT_HORIZONS
        float aiDh = texture2D(dhDepthTex1, uv).r;
        #ifdef NETHER
            if (texture2D(colortex15, uv).r < 0.5) aiDh = 1.0;
        #endif
        if (aiDh < 1.0 && (!aiSurface || length(aiView.xyz) > 128.0)) {
            vec4 aiDhView = dhProjectionInverse * vec4(vec3(uv, aiDh) * 2.0 - 1.0, 1.0);
            aiDhView /= aiDhView.w;
            if (!aiSurface || length(aiDhView.xyz) < length(aiView.xyz)) { aiView = aiDhView; aiSurface = true; }
        }
    #endif
    vec3 aiEnd = (gbufferModelViewInverse * vec4(aiView.xyz, 1.0)).xyz;
    float aiDistance = length(aiEnd);
    vec3 aiRay = aiEnd / max(aiDistance, 0.001);
    // Begin outside the hull, preserving the clear cabin and window frame.
    float aiStart = 0.0;
    float aiHull = texture2D(aiVortexHull, uv).r;
    bool aiCabin = aiHull < 1.0;
    if (aiCabin) {
        vec4 aiHullView = gbufferProjectionInverse * vec4(vec3(uv, aiHull) * 2.0 - 1.0, 1.0);
        aiStart = length(aiHullView.xyz / aiHullView.w);
    }
    bool aiLavaMedium = isEyeInWater == 2 || (aiCabin && aiVortexTuning.w > 1.5);
    // No first-person hands/items or sky surfaces receive diffuse light.
    if (aiSurface && frontDepth > 0.56 && aiDistance < 112.0) {
        vec3 aiNormal = normalize(cross(dFdx(aiEnd), dFdy(aiEnd)));
        if (dot(aiNormal, aiRay) > 0.0) aiNormal = -aiNormal;
        vec3 aiPoint = aiEnd;
        if (aiVortexTuning.z > 0.5) aiPoint = (floor((aiPoint + cameraPosition) * 16.0) + 0.5) / 16.0 - cameraPosition;
        vec3 aiLight = AiVortexLight(aiPoint + aiNormal * 0.5);
        color += aiLight * aiVortexTuning.x * (aiLavaMedium ? exp(-max(aiDistance - aiStart, 0.0) * 0.16) : 1.0) * (0.04 + 0.65 * clamp(color, 0.0, 1.0));
    }
    if (aiStart >= aiDistance) return;
    // Refraction distance starts at visible water when viewed from air.
    bool aiWaterSurface = int(texture2D(colortex6, uv).g * 255.1) == 241;
    bool aiFluid = isEyeInWater == 1 || isEyeInWater == 2 || aiCabin || aiWaterSurface;
    if (isEyeInWater == 0 && !aiCabin && aiWaterSurface && frontDepth < backDepth) {
        vec4 aiFront = gbufferProjectionInverse * vec4(vec3(uv, frontDepth) * 2.0 - 1.0, 1.0);
        aiStart = length(aiFront.xyz / aiFront.w);
    }
    if (aiVortexGrid.w < 0.5 && aiMistCount == 0) return;
    if (!aiFluid) {
        bool aiVisibleMist = false;
        for (int m = 0; m < 16; ++m) {
            if (m >= aiMistCount) break;
            float aiClosest = clamp(dot(aiMistPosition[m].xyz, aiRay), aiStart, min(aiDistance, 112.0));
            if (length(aiRay * aiClosest - aiMistPosition[m].xyz) < aiMistPosition[m].w) aiVisibleMist = true;
        }
        if (!aiVisibleMist) return;
    }
    float aiLength = max(min(aiDistance, 112.0) - aiStart, 0.0);
    if (aiLength <= 0.001) return;
    vec3 aiGlow = vec3(0.0), aiMist = vec3(0.0);
    float aiOpacity = 0.0;
    // Fixed 6-tap budget; actual grid bounds/geometry prevent lighting through solid blocks.
    for (int i = 0; i < 6; ++i) {
        float aiT = aiStart + aiLength * (float(i) + 0.5) / 6.0;
        vec3 aiP = aiRay * aiT;
        vec3 aiLit = AiVortexLight(aiP);
        if (aiFluid) aiGlow += aiLit * (aiLength / 6.0) * 0.009;
        for (int m = 0; m < 16; ++m) {
            if (m >= aiMistCount) break;
            float aiR = max(aiMistPosition[m].w, 0.01);
            float aiD = length(aiP - aiMistPosition[m].xyz) / aiR;
            if (aiD >= 1.0) continue;
            float aiDensity = max(1.0 - aiD * aiD, 0.0) * max(aiMistColor[m].a, 0.0);
            aiDensity *= (0.35 + 0.65 * AiVortexNoise(aiP + cameraPosition)) * aiLength / 6.0 * 0.025;
            float aiStep = (1.0 - exp(-aiDensity)) * (1.0 - aiOpacity);
            aiMist += aiStep * aiMistColor[m].rgb * (0.15 + aiLit * aiVortexTuning.x);
            aiOpacity += aiStep;
        }
    }
    // Lava is much more opaque than water: avoid beams piercing distant lava fog.
    float aiAbsorption = aiLavaMedium ? exp(-aiLength * 0.12) : 1.0;
    color = color * (1.0 - aiOpacity) + aiMist + min(aiGlow, vec3(1.5)) * aiVortexTuning.y * aiAbsorption;
}
