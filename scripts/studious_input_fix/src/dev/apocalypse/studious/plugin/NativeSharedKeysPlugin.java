package dev.apocalypse.studious.plugin;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Preserve NeoForge dispatch; neutralize only the two redundant Fabric callbacks. */
public final class NativeSharedKeysPlugin implements IMixinConfigPlugin {
    public void onLoad(String pkg){}
    public String getRefMapperConfig(){return null;}
    public boolean shouldApplyMixin(String target,String mixin){return true;}
    public void acceptTargets(Set<String>mine,Set<String>others){}
    public List<String> getMixins(){return null;}
    public void preApply(String name,ClassNode target,String mixin,IMixinInfo info){}
    public void postApply(String name,ClassNode target,String mixin,IMixinInfo info){
        if(!name.equals("net.minecraft.client.KeyMapping"))throw new IllegalStateException("Unexpected target "+name);
        int patched=0;
        for(var method:target.methods){
            boolean click=method.name.endsWith("$studiouskeybinds$clickShared");
            boolean set=method.name.endsWith("$studiouskeybinds$setShared");
            if(!click&&!set)continue;
            String expected="(Lcom/mojang/blaze3d/platform/InputConstants$Key;"+(set?"Z":"")+"Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V";
            if(!method.desc.equals(expected)||(method.access&Opcodes.ACC_STATIC)==0)throw new IllegalStateException("Unrecognized Studious callback "+method.name+method.desc);
            method.instructions.clear();
            method.instructions.add(new InsnNode(Opcodes.RETURN));
            method.tryCatchBlocks.clear();method.localVariables=null;method.maxStack=0;
            patched++;
        }
        if(patched!=2)throw new IllegalStateException("Expected two Studious 1.0.8-beta.1 callbacks, found "+patched);
        System.out.println("STUDIOUS_NATIVE_SHARED_KEYS: two Fabric callbacks adapted to native NeoForge dispatch");
    }
}
