package dev.apocalypse.pp;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.util.RandomSource;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
public final class EchoRecipeTest {
 private static int checks;
 private static void check(boolean ok,String text){if(!ok)throw new IllegalStateException("PP selftest: Echo recipe "+text);checks++;}
 public static int run(CommandSourceStack source) throws Exception {
  checks=0;var id=ResourceLocation.parse("apocalypse_pp:echo_plate_assembly");
  var holder=source.getServer().getRecipeManager().byKey(id).orElseThrow();
  check(holder.value() instanceof SequencedAssemblyRecipe,"native assembly loaded");
  var recipe=(SequencedAssemblyRecipe)holder.value();
  var plate=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("protection_pixel:alloyarmorplate")));
  check(recipe.getIngredient().test(plate),"fresh Alloy accepted");
  plate.setDamageValue(1);check(!recipe.getIngredient().test(plate),"slightly damaged Alloy rejected");
  plate.setDamageValue(99);check(!recipe.getIngredient().test(plate),"exhausted Alloy rejected");
  plate.setDamageValue(0);CustomData.update(DataComponents.CUSTOM_DATA,plate,n->n.putDouble("armor",1.5));
  check(recipe.getIngredient().test(plate),"fresh Alloy with native stat metadata accepted");
  check(!recipe.getIngredient().test(new ItemStack(ApocalypsePP.ECHO_PLATE.get())),"wrong plate rejected");
  check(recipe.getLoops()==8 && recipe.getSequence().size()==8 && recipe.getOutputChance()==1,"eight loops, eight stages, no failure");
  var advance=SequencedAssemblyRecipe.class.getDeclaredMethod("advance",ResourceLocation.class,ItemStack.class,RandomSource.class);advance.setAccessible(true);
  var next=SequencedAssemblyRecipe.class.getDeclaredMethod("getNextRecipe",ItemStack.class);next.setAccessible(true);
  var applies=SequencedAssemblyRecipe.class.getDeclaredMethod("appliesTo",ResourceLocation.class,ItemStack.class);applies.setAccessible(true);
  String[] stages={"filling","pressing","filling","deploying","pressing","filling","deploying","pressing"};
  ItemStack working=plate;var random=RandomSource.create(421);
  for(int i=0;i<64;i++) {
   check((boolean)applies.invoke(recipe,id,working),"valid input at stage "+i);
   var step=((SequencedRecipe<?>)next.invoke(recipe,working)).getRecipe();
   check(BuiltInRegistries.RECIPE_SERIALIZER.getKey(step.getSerializer()).toString().equals("create:"+stages[i%8]),"operation order at stage "+i);
   working=(ItemStack)advance.invoke(recipe,id,working,random);
   check(working.is(i==63?ApocalypsePP.UNFINISHED_ECHO_PLATE.get():ApocalypsePP.INCOMPLETE_ECHO_PLATE.get()),"no early basin intermediate at stage "+i);
  }
  check(!working.has(com.simibubi.create.AllDataComponents.SEQUENCED_ASSEMBLY),"finished intermediate has no unfinished progress");
  var ops=source.registryAccess().createSerializationContext(JsonOps.INSTANCE);
  var finish=source.getServer().getRecipeManager().byKey(ResourceLocation.parse("apocalypse_pp:echo_plate")).orElseThrow().value();
  var encoded=Recipe.CODEC.encodeStart(ops,finish).getOrThrow().getAsJsonObject();
  check(encoded.get("type").getAsString().equals("create:compacting") && encoded.get("heat_requirement").getAsString().equals("heated"),"native finish requires heated compacting");
  check(finish.getIngredients().size()==3,"three basin ingredients");
  check(finish.getIngredients().stream().anyMatch(x->x.test(workingCopy())),"basin accepts completed intermediate");
  check(finish.getIngredients().stream().noneMatch(x->x.test(new ItemStack(ApocalypsePP.INCOMPLETE_ECHO_PLATE.get()))),"basin rejects transition item");
  check(finish.getResultItem(source.registryAccess()).is(ApocalypsePP.ECHO_PLATE.get()),"basin outputs final plate");
  return checks;
 }
 private static ItemStack workingCopy(){return new ItemStack(ApocalypsePP.UNFINISHED_ECHO_PLATE.get());}
}
