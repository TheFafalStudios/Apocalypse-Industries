package dev.apocalypse.envelope;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class Settings {
 public static final ModConfigSpec SPEC;
 public static final ModConfigSpec.BooleanValue ENABLED;
 public static final ModConfigSpec.DoubleValue ENVELOPE_CLEARANCE,HULL_CLEARANCE,MARGIN,MAX_RADIUS,CHANCE;
 public static final ModConfigSpec.IntValue INTERVAL,COOLDOWN;
 static {
  var b=new ModConfigSpec.Builder();
  ENABLED=b.comment("False restores upstream envelope destruction.").define("enabled",true);
  ENVELOPE_CLEARANCE=b.comment("Minimum envelope height above local terrain.").defineInRange("minimumEnvelopeClearance",30.0,0.0,512.0);
  HULL_CLEARANCE=b.comment("Minimum lower ship bounding-box clearance above terrain sampled at center and four corners.").defineInRange("minimumHullClearance",5.0,0.0,128.0);
  MARGIN=b.comment("Extra blocks around actual server funnel layers, including vertical end tolerance.").defineInRange("funnelMargin",8.0,0.0,32.0);
  MAX_RADIUS=b.comment("Maximum horizontal puncture radius, including the margin.").defineInRange("maximumDamageRadius",32.0,1.0,128.0);
  CHANCE=b.comment("Maximum chance per eligible opportunity; quadratic falloff to zero at radius.").defineInRange("maximumChance",0.10,0.0,1.0);
  INTERVAL=b.comment("Minimum ticks between opportunities, shared across all storms and envelope candidates.").defineInRange("opportunityIntervalTicks",100,1,12000);
  COOLDOWN=b.comment("Ship-wide cooldown after a successful single-block puncture.").defineInRange("successfulPunctureCooldownTicks",600,1,12000);
  SPEC=b.build();
 }
 private Settings() {}
}
