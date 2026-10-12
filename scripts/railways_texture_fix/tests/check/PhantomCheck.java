package check;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.railwayteam.railways.content.custom_tracks.phantom.PhantomSpriteManager;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod(value="phantom_check",dist=Dist.CLIENT)
public class PhantomCheck {
    public static boolean fixture=false;
    private int ticks=0, phase=0;
    public PhantomCheck(){ NeoForge.EVENT_BUS.addListener(this::tick); }
    private void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(phase==0 && mc.screen!=null && mc.getOverlay()==null){
            if(++ticks<60)return;
            phase=1;
            try{
                mc.getTextureManager().bindForSetup(TextureAtlas.LOCATION_BLOCKS);
                mc.setOverlay(new LoadingOverlay(mc,null,ignored->{},false));
                fixture=true;
                PhantomSpriteManager.hasChanged=true;
                try { PhantomSpriteManager.renderTick(); }
                catch(ArrayIndexOutOfBoundsException expected){
                    System.out.println("PHANTOM_CHECK BASELINE_REPRODUCED: short captured mip array + completed mip chain crashes original upload during reload");
                    mc.setOverlay(null); fixture=false; mc.stop(); return;
                }
                if(!PhantomSpriteManager.hasChanged) throw new IllegalStateException("Pending upload cleared during reload");
                System.out.println("PHANTOM_CHECK reload upload deferred; pending visibility preserved");
                mc.setOverlay(null);
                fixture=false;
                PhantomSpriteManager.renderTick();
                if(PhantomSpriteManager.hasChanged) throw new IllegalStateException("Upload did not resume after reload");
                System.out.println("PHANTOM_CHECK real phantom textures uploaded after reload");
                // Exercise both visible/hidden frame selections with actual loaded sprites.
                var f=PhantomSpriteManager.class.getDeclaredField("lastVisible");f.setAccessible(true);
                for(boolean visible:new boolean[]{true,false,true,false}){
                    f.setBoolean(null,visible);PhantomSpriteManager.hasChanged=true;
                    PhantomSpriteManager.renderTick();
                }
                System.out.println("PHANTOM_CHECK visibility frame toggles passed");
                mc.reloadResourcePacks().thenRun(()->System.out.println("PHANTOM_CHECK real resource reload completed"));
                ticks=0;
            }catch(Throwable t){System.out.println("PHANTOM_CHECK FAIL");t.printStackTrace();mc.setOverlay(null);mc.stop();}
        }else if(phase==1 && mc.getOverlay()==null && ++ticks>80){
            System.out.println("PHANTOM_CHECK PASS: deferred race fixture, resumed upload, four visibility changes, full resource reload and stable client ticks");
            phase=2;mc.stop();
        }
    }
}
