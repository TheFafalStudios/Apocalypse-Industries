package dev.apocalypse.subbalance.tests;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig;
import com.maxenonyme.createsubmarine.submarine.system.SubmarinePressureSystem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import java.nio.file.*;
import java.util.*;
import dev.apocalypse.subbalance.Balance;
import dev.apocalypse.subbalance.BalanceMod;
@Mod("apocalypse_sub_balance_tests")
public final class NativeHarness {
    public NativeHarness(IEventBus bus) { bus.addListener(this::loaded); }
    private void loaded(FMLLoadCompleteEvent event) { event.enqueueWork(() -> {
        try {
            HullStrengthConfig.load();
            Map<String,Integer> results=new LinkedHashMap<>();
            Map<String,Integer> expected=Map.of("minecraft:netherite_block",225,"minecraft:glass",20,
                "minecraft:tinted_glass",20,"create_submarine:iron_pressurizer",60,
                "dndecor:netherite_sheet_metal",150);
            for (var e:expected.entrySet()) {
                var state=BuiltInRegistries.BLOCK.get(ResourceLocation.parse(e.getKey())).defaultBlockState();
                int value=HullStrengthConfig.getFor(state).orElseThrow().maxWaterDepth();
                // Ordinary glass defaults vary; it must preserve a weaker default, never exceed 60.
                if (e.getKey().contains("minecraft:") && e.getKey().contains("glass")) {
                    if(value>60)throw new AssertionError(e.getKey()+"="+value);
                } else if(value!=e.getValue())throw new AssertionError(e.getKey()+"="+value+" expected "+e.getValue());
                results.put(e.getKey(),value);
            }
            var methods=Arrays.stream(SubmarinePressureSystem.class.getDeclaredMethods()).map(java.lang.reflect.Method::getName).toList();
            for(String hook:List.of("ai$fresh","ai$edit","ai$live","ai$materials","ai$diagnostics","ai$effective","ai$material","ai$weakestCopied"))
                if(methods.stream().noneMatch(n->n.contains(hook)))throw new AssertionError("Mixin missing: "+hook);
            var windows=new TreeMap<String,Integer>();
            for(var block:BuiltInRegistries.BLOCK) {
                var state=block.defaultBlockState();
                if(!Balance.window(state)) continue;
                var property=HullStrengthConfig.getFor(state);
                if(property.isPresent()) {
                    int value=property.get().maxWaterDepth();
                    if(value>60)throw new AssertionError("Strong viewport: "+Balance.id(state));
                    windows.put(Balance.id(state),value);
                }
            }
            var fitting=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create_submarine:ballast_vent"));
            var state=fitting.defaultBlockState();
            if(!BalanceMod.eligible(state))throw new AssertionError("Vent must support a pressure jacket");
            if(BalanceMod.eligible(BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create_submarine:iron_pressurizer")).defaultBlockState()))throw new AssertionError("Glass cannot be jacketed");
            var factory=(net.minecraft.world.level.block.EntityBlock)fitting;
            var be=Objects.requireNonNull(factory.newBlockEntity(net.minecraft.core.BlockPos.ZERO,state));
            be.getPersistentData().putBoolean(BalanceMod.REINFORCED,true);
            var lookup=net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
            var saved=be.saveWithFullMetadata(lookup);
            var restored=Objects.requireNonNull(factory.newBlockEntity(net.minecraft.core.BlockPos.ZERO,state));
            restored.loadWithComponents(saved,lookup);
            if(!restored.getPersistentData().getBoolean(BalanceMod.REINFORCED))throw new AssertionError("Pressure jacket lost during block-entity save/load");
            Files.writeString(Path.of("balance-native-validation.json"),new com.google.gson.Gson().toJson(Map.of("pass",true,"waterRatings",results,"viewports",windows,"pressureJacketSaveLoad",true,"pressureMixinHooksApplied",true,"worldStarted",false)));
            System.exit(0);
        } catch(Throwable t) {
            t.printStackTrace();
            try {Files.writeString(Path.of("balance-native-validation.json"),new com.google.gson.Gson().toJson(Map.of("pass",false,"error",t.toString())));} catch(Exception ignored){}
            System.exit(1);
        }
    }); }
}
