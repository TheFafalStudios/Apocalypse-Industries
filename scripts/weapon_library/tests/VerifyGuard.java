import java.lang.reflect.*;
import java.util.regex.Pattern;
import java.util.function.Supplier;
import apocalypse.tridents.mixin.PolytoneNameGuardMixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
public class VerifyGuard {
 public static void main(String[] args) throws Exception {
  ClassLoader loader=VerifyGuard.class.getClassLoader();
  Class<?> stack=Class.forName("net.minecraft.world.item.ItemStack",false,loader);
  Class<?> level=Class.forName("net.minecraft.world.level.Level",false,loader);
  Class<?> component=Class.forName("net.minecraft.network.chat.Component",false,loader);
  var target=Class.forName("net.mehvahdjukaar.polytone.content.item.ItemModelOverride",false,loader);
  target.getDeclaredMethod("matchesPredicate",stack,level,Supplier.class,component);
  if(target.getDeclaredField("pattern").getType()!=Pattern.class)throw new AssertionError("Shadow mismatch");
  var instance=new PolytoneNameGuardMixin();
  var field=PolytoneNameGuardMixin.class.getDeclaredField("pattern");field.setAccessible(true);
  var method=PolytoneNameGuardMixin.class.getDeclaredMethod("requireCustomName",stack,level,Supplier.class,component,CallbackInfoReturnable.class);method.setAccessible(true);
  var unguarded=new CallbackInfoReturnable<Boolean>("matchesPredicate",true,true);
  method.invoke(instance,null,null,null,null,unguarded);
  if(unguarded.isCancelled())throw new AssertionError("Non-name override affected");
  field.set(instance,Pattern.compile(".*Crystal Trident.*"));
  var guarded=new CallbackInfoReturnable<Boolean>("matchesPredicate",true,true);
  method.invoke(instance,null,null,null,null,guarded);
  if(!guarded.isCancelled()||guarded.getReturnValue())throw new AssertionError("Unnamed item was not rejected");
  System.out.println("PASS: installed Polytone target signature and shadow verified; unnamed patterned items rejected; non-name overrides unaffected.");
 }
}
