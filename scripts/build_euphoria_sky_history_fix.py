from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import hashlib,json
root=Path.cwd()
src=root/'shaderpacks/ComplementaryUnbound_r5.8.1_EuphoriaPatches_1.9.3_APOCALYPSE_ATMOSPHERE_v1.1.zip'
out=src.with_name(src.stem+'_SKY_HISTORY_FIX_v1_TEST.zip')
assert not out.exists(), 'Test pack already exists'
with ZipFile(src) as z: original={n:z.read(n) for n in z.namelist()}
data=original.copy()
helper='''// Apocalypse sky history v1: categorical coverage in unused normal-TAA alpha.
// Special history modes retain their original implementation.
#if LONG_EXPOSURE == 0 && !defined RENKO_CUT
#define APOCALYPSE_SKY_HISTORY
float ApocalypseHistoryClass(ivec2 pixel) {
    float frontDepth = texelFetch(depthtex0, pixel, 0).r;
    float solidDepth = texelFetch(depthtex1, pixel, 0).r;
    int mask = int(texelFetch(colortex6, pixel, 0).g * 255.1);
    if (frontDepth < solidDepth || mask == 251 || mask == 254 ||
        abs(float(mask) - 149.5) < 50.0) return 0.25;
    bool skyPixel = frontDepth == 1.0 && solidDepth == 1.0;
    #ifdef VOXY
        skyPixel = skyPixel && texelFetch(vxDepthTexOpaque, pixel, 0).r == 1.0;
    #elif defined DISTANT_HORIZONS
        skyPixel = skyPixel && texelFetch(dhDepthTex1, pixel, 0).r == 1.0;
    #endif
    return skyPixel ? 1.0 : 0.5;
}
bool ApocalypseHistoryValid(vec2 previousUV, float currentClass) {
    if (any(lessThanEqual(previousUV, vec2(0.0))) ||
        any(greaterThanEqual(previousUV, vec2(1.0)))) return false;
    ivec2 base = ivec2(floor(previousUV * view - 0.5));
    // Validate discrete classes throughout the history interpolation footprint.
    #if TAA_MOVEMENT_IMPROVEMENT_FILTER == 1
        const int firstTap = -1;
        const int lastTap = 2;
    #else
        const int firstTap = 0;
        const int lastTap = 1;
    #endif
    for (int y = firstTap; y <= lastTap; ++y) {
        for (int x = firstTap; x <= lastTap; ++x) {
            ivec2 pixel = clamp(base + ivec2(x, y), ivec2(0), ivec2(view) - 1);
            float previousClass = texelFetch(colortex2, pixel, 0).a;
            if (abs(previousClass - currentClass) > 0.1) return false;
        }
    }
    return true;
}
#endif

'''
def replace(s,a,b):
    assert s.count(a)==1, a
    return s.replace(a,b,1)
p='shaders/lib/antialiasing/taa.glsl'
s=original[p].decode().replace('\r','')
s=replace(s,'        vec3 clr = texelFetch(colortex3, texelCoordM2, 0).rgb;','''        #ifdef APOCALYPSE_SKY_HISTORY
            // Do not widen sky colour bounds using foreground neighbours.
            bool centerSky = ApocalypseHistoryClass(texelCoord) > 0.75;
            bool neighbourSky = ApocalypseHistoryClass(texelCoordM2) > 0.75;
            if (centerSky != neighbourSky) continue;
        #endif
        vec3 clr = texelFetch(colortex3, texelCoordM2, 0).rgb;''')
s=replace(s,'    #if TAA_MOVEMENT_IMPROVEMENT_FILTER == 1\n        vec3 tempColor','''    #ifdef APOCALYPSE_SKY_HISTORY
        if (!ApocalypseHistoryValid(prvCoord, ApocalypseHistoryClass(texelCoord))) {
            temp = color;
            return;
        }
    #endif
    #if TAA_MOVEMENT_IMPROVEMENT_FILTER == 1
        vec3 tempColor''')
data[p]=(helper+s).encode()
p2='shaders/program/composite6.glsl'
s=original[p2].decode().replace('\r','')
s=replace(s,'    gl_FragData[1] = vec4(temp, 0.0);','''    #ifdef APOCALYPSE_SKY_HISTORY
        gl_FragData[1] = vec4(temp, ApocalypseHistoryClass(texelCoord));
    #else
        gl_FragData[1] = vec4(temp, 0.0);
    #endif''')
data[p2]=s.encode()
assert {n for n in data if data[n]!=original[n]}=={p,p2}
with ZipFile(out,'w',ZIP_DEFLATED) as z:
    for n,b in data.items(): z.writestr(n,b)
with ZipFile(out) as z:
    assert z.testzip() is None
    assert set(z.namelist())==set(original)
    assert all(z.read(n)==b for n,b in data.items())
settings=Path(str(src)+'.txt')
Path(str(out)+'.txt').write_bytes(settings.read_bytes())
sha=lambda b:hashlib.sha256(b).hexdigest()
report={'source':src.name,'source_sha256':sha(src.read_bytes()),'candidate':out.name,'candidate_sha256':sha(out.read_bytes()),'changes':{n:{'before':sha(original[n]),'after':sha(data[n])} for n in (p,p2)},'validation':'ZIP CRC and full entry comparison passed. GPU compilation and gameplay not tested.','limitations':['Categorical visibility history, not full previous-depth reconstruction','Conservative rejection can shimmer at silhouettes','Additional texture fetches may cost GPU time','Particles without depth or material coverage can still trail','Long exposure and Renko cut intentionally bypass the experiment']}
(root/'docs/EUPHORIA_SKY_HISTORY_FIX_v1.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
