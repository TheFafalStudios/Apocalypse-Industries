package check;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

@Mod(value="studious_input_check",dist=Dist.CLIENT)
public class InputCheck {
    private int ticks=0,phase=0,checks=0;
    public InputCheck(){NeoForge.EVENT_BUS.addListener(this::tick);}
    private void verify(boolean condition,String label){if(!condition)throw new AssertionError(label);checks++;}
    private Button keyboard(GuiEventListener x){
        if(x instanceof Button b&&b.getMessage().getString().equalsIgnoreCase("Keyboard"))return b;
        if(x instanceof ContainerEventHandler c)for(var child:c.children()){var b=keyboard(child);if(b!=null)return b;}
        return null;
    }
    private void shared(InputConstants.Type type,int code){
        var key=type.getOrCreate(code);
        var a=new KeyMapping("inputcheck.a"+code,KeyConflictContext.UNIVERSAL,key,"inputcheck");
        var b=new KeyMapping("inputcheck.b"+code,KeyConflictContext.UNIVERSAL,key,"inputcheck");
        var inactive=new IKeyConflictContext(){public boolean isActive(){return false;}public boolean conflicts(IKeyConflictContext other){return other==this;}};
        var c=new KeyMapping("inputcheck.inactive"+code,inactive,key,"inputcheck");
        KeyMapping.resetMapping();
        for(int i=0;i<5;i++){
            KeyMapping.set(key,true);verify(a.isDown()&&b.isDown(),"Shared press");verify(!c.isDown(),"Inactive context stays inactive");
            KeyMapping.click(key);verify(a.consumeClick()&&b.consumeClick(),"Both shared actions click");
            verify(!a.consumeClick()&&!b.consumeClick(),"One click per action without duplicates");
            KeyMapping.set(key,false);verify(!a.isDown()&&!b.isDown(),"Shared release");
        }
        var replacement=type.getOrCreate(code+1);
        b.setKey(replacement);KeyMapping.resetMapping();
        KeyMapping.set(key,true);verify(a.isDown()&&!b.isDown(),"Rebind isolates old key");KeyMapping.set(key,false);
        KeyMapping.set(replacement,true);verify(b.isDown()&&!a.isDown(),"Rebound key works");KeyMapping.set(replacement,false);
        b.setKey(InputConstants.UNKNOWN);KeyMapping.resetMapping();
        KeyMapping.set(replacement,true);verify(!b.isDown(),"Unbound action remains up");KeyMapping.set(replacement,false);
    }
    private void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(phase==0&&mc.screen!=null&&mc.getOverlay()==null){
            if(++ticks<40)return;
            phase=1;
            try{
                var left=new KeyMapping("inputcheck.left",KeyConflictContext.UNIVERSAL,InputConstants.Type.MOUSE,0,"inputcheck");
                KeyMapping.resetMapping();
                try{KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(0),true);}
                catch(ClassCastException expected){
                    if(!expected.getMessage().contains("CopyOnWriteArrayList"))throw expected;
                    System.out.println("STUDIOUS_INPUT_CHECK BASELINE_REPRODUCED: mouse press CopyOnWriteArrayList/ArrayList crash");mc.stop();return;
                }
                KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(0),false);
                System.out.println("STUDIOUS_INPUT_CHECK real mouse press/release passed");
                shared(InputConstants.Type.KEYSYM,312);
                shared(InputConstants.Type.MOUSE,6);
                // Test fixture mappings above exercise actual dispatch without invoking
                // unrelated mod callbacks that require a connected server/player.
                System.out.println("STUDIOUS_INPUT_CHECK shared keys, context filtering, single clicks, rebinding and unbinding passed; assertions="+checks);
                var controls=new ControlsScreen(mc.screen,mc.options);mc.setScreen(controls);
                var button=keyboard(controls);verify(button!=null,"Studious button retained");button.onPress();
                verify(mc.screen.getClass().getName().equals("com.studiousgoose.keybinds.client.KeyboardScreen"),"Studious editor retained");
                ticks=0;
            }catch(Throwable t){System.out.println("STUDIOUS_INPUT_CHECK FAIL");t.printStackTrace();mc.stop();}
        }else if(phase==1&&++ticks>=60){
            System.out.println("STUDIOUS_INPUT_CHECK PASS: native key input and Studious editor; assertions="+checks);phase=2;mc.stop();
        }
    }
}
