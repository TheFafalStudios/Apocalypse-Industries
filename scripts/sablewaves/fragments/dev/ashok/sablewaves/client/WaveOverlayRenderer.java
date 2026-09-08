// A compilation fragment: only named methods are transplanted by Patch.java.
// The original renderer and its record class are otherwise retained.
package dev.ashok.sablewaves.client;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ashok.sablewaves.hotfix.ColorBlend;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
public final class WaveOverlayRenderer {
    private Map<Long, Column> columns;
    private Map<Long, Long> wetSand;
    private ClientLevel oceanLevel;
    private long lastFrameNano, lastWindSampleTick;
    private double smoothTime, renderMsAvg;
    private int errorCount, qualityLevel, qualityCooldown, qualityUpPenalty, qualitySinceUp;
    private TextureAtlasSprite waterSprite;
    private int spriteAttempts;
    private float spriteU0, spriteU1, spriteV0, spriteV1;
    private record Column(float surfaceY, int waterColor, float depth, float shoreDist, float groundY, long stamp) {}

    private void hotfixPrepare(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (oceanLevel != Minecraft.getInstance().level) hotfixReset();
    }
    private void hotfixReset() {
        columns.clear(); wetSand.clear();
        lastFrameNano = 0; lastWindSampleTick = Long.MIN_VALUE; smoothTime = 0;
        errorCount = 0; qualityLevel = 0; qualityCooldown = 0;
        qualityUpPenalty = 0; qualitySinceUp = 0x3fffffff; renderMsAvg = 0;
        waterSprite = null; spriteAttempts = 0;
    }

    private boolean acquireSprite() {
        // Re-read the atlas entry each frame so F3+T/resource-pack changes cannot
        // leave this renderer holding obsolete UVs or stale biome tint columns.
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(ResourceLocation.withDefaultNamespace("block/water_still"));
        if (sprite == null) return false;
        if (sprite != waterSprite) {
            waterSprite = sprite;
            spriteU0 = sprite.getU0(); spriteU1 = sprite.getU1();
            spriteV0 = sprite.getV0(); spriteV1 = sprite.getV1();
            columns.clear(); wetSand.clear();
        }
        return true;
    }

    private static long hotfixKey(int x, int z) {
        return (((long)x << 32) ^ (long)z) * -7046029254386353131L;
    }
    private int hotfixColor(double x, double z, int fallback) {
        int bx = (int)Math.floor(x - 0.5), bz = (int)Math.floor(z - 0.5);
        Column a = columns.get(hotfixKey(bx, bz));
        Column b = columns.get(hotfixKey(bx + 1, bz));
        Column c = columns.get(hotfixKey(bx, bz + 1));
        Column d = columns.get(hotfixKey(bx + 1, bz + 1));
        // Nearest available water sample fills missing/land samples, so shore
        // edges do not pick up the fallback blue stored for dry terrain.
        int seed = a != null && !Float.isNaN(a.surfaceY()) ? a.waterColor()
                 : b != null && !Float.isNaN(b.surfaceY()) ? b.waterColor()
                 : c != null && !Float.isNaN(c.surfaceY()) ? c.waterColor()
                 : d != null && !Float.isNaN(d.surfaceY()) ? d.waterColor() : fallback;
        return ColorBlend.blend(hotfixColorOf(a, seed), hotfixColorOf(b, seed),
            hotfixColorOf(c, seed), hotfixColorOf(d, seed), x - 0.5 - bx, z - 0.5 - bz);
    }
    private static int hotfixColorOf(Column column, int fallback) {
        return column == null || Float.isNaN(column.surfaceY()) ? fallback : column.waterColor();
    }
    private void hotfixRestoreState() {
        RenderSystem.enableCull(); RenderSystem.depthMask(true); RenderSystem.disableBlend();
    }
}
