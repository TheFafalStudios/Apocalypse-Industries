import java.lang.reflect.*;
import java.util.*;
import dev.ashok.sablewaves.hotfix.ColorBlend;
public final class RendererTests {
    static int checks;
    static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        check(ColorBlend.blend(0,0xffffff,0,0xffffff,0.5,0.5)==0x808080,"midpoint");
        check(ColorBlend.blend(0x123456,0xffffff,0,0,0,0)==0x123456,"corner");
        check(ColorBlend.blend(0x123456,0x123456,0x123456,0x123456,0.37,0.71)==0x123456,"constant");
        Class<?> renderer = Class.forName("dev.ashok.sablewaves.client.WaveOverlayRenderer");
        Object instance = renderer.getConstructor().newInstance();
        Class<?> column = Class.forName("dev.ashok.sablewaves.client.WaveOverlayRenderer$Column");
        Constructor<?> constructor = column.getDeclaredConstructors()[0]; constructor.setAccessible(true);
        Field field = renderer.getDeclaredField("columns"); field.setAccessible(true);
        @SuppressWarnings("unchecked") Map<Long,Object> columns = (Map<Long,Object>)field.get(instance);
        Method key = renderer.getDeclaredMethod("hotfixKey",int.class,int.class); key.setAccessible(true);
        Method color = renderer.getDeclaredMethod("hotfixColor",double.class,double.class,int.class); color.setAccessible(true);
        for (int origin : new int[]{-30000000,-17,-1,0,15,30000000}) {
            columns.clear();
            for (int x=origin-1;x<=origin+1;x++) for (int z=-1;z<=1;z++) {
                int tint=x < origin ? 0x204060 : 0xa0c0e0;
                columns.put((Long)key.invoke(null,x,z),constructor.newInstance(63.9f,tint,6f,99f,Float.NaN,1L));
            }
            int left=(int)color.invoke(instance,(double)origin,0.0,0x204060);
            int right=(int)color.invoke(instance,(double)origin,0.0,0xa0c0e0);
            check(left==right,"shared vertex independent of owning block at "+origin);
            check(left==0x6080a0,"biome edge blend at "+origin);
            int before=(int)color.invoke(instance,origin-0.00001,0.0,0);
            int after=(int)color.invoke(instance,origin+0.00001,0.0,0);
            check(before==after,"no discontinuity at "+origin);
        }
        Map<Integer,Integer> buckets = new HashMap<>();
        Set<Long> keys = new HashSet<>();
        for(int x=-128;x<=128;x++) for(int z=-128;z<=128;z++) {
            long k=(long)key.invoke(null,x,z); check(keys.add(k),"unique coordinate key");
            int h=Long.hashCode(k); h ^= h >>> 16; buckets.merge(h & 131071,1,Integer::sum);
        }
        int max=Collections.max(buckets.values()); check(max<32,"cache hashing max bucket="+max);
        Field wetField=renderer.getDeclaredField("wetSand");wetField.setAccessible(true);
        @SuppressWarnings("unchecked") Map<Long,Long> wet=(Map<Long,Long>)wetField.get(instance);
        wet.put(42L,123L);
        Field errors=renderer.getDeclaredField("errorCount");errors.setAccessible(true);errors.setInt(instance,6);
        Field windTick=renderer.getDeclaredField("lastWindSampleTick");windTick.setAccessible(true);windTick.setLong(instance,500);
        Method reset=renderer.getDeclaredMethod("hotfixReset");reset.setAccessible(true);reset.invoke(instance);
        check(columns.isEmpty()&&wet.isEmpty(),"world reset clears terrain and wet sand");
        check(errors.getInt(instance)==0,"new world retries renderer after previous errors");
        check(windTick.getLong(instance)==Long.MIN_VALUE,"new world resamples wind even at same tick");
        System.out.println("Renderer/colour/hash/lifecycle checks="+checks+", maximum bucket="+max);
    }
}
