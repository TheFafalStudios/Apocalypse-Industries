package dev.apocalypse.envelope;
/** Geometry and scheduling used by the runtime policy. */
public final class DamageMath {
 public static double probability(double distance,double radius,double maximum) {
  if(!Double.isFinite(distance)||!Double.isFinite(radius)||radius<=0||distance<0||distance>=radius)return 0;
  double proximity=1-distance/radius;return maximum*proximity*proximity;
 }
 public static final class Gate {
  private long next=Long.MIN_VALUE,last=Long.MIN_VALUE;
  public boolean ready(long tick) {
   if(tick<last)next=Long.MIN_VALUE;last=tick;return tick>=next;
  }
  public boolean begin(long tick,int interval) {
   if(!ready(tick))return false;next=tick+interval;return true;
  }
  public void success(long tick,int cooldown) {next=Math.max(next,tick+cooldown);}
 }
 private DamageMath() {}
}
