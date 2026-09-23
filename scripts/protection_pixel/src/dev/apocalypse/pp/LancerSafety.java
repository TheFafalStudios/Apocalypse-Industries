package dev.apocalypse.pp;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
public final class LancerSafety {
    private record Sample(ItemStack helmet, String dimension, int tick, Vec3 position) {}
    private static final Map<Player, Sample> SAMPLES = new WeakHashMap<>();
    private static boolean lancer(ItemStack s) { String id = ApocalypsePP.id(s); return id.equals("protection_pixel:lancer_helmet") || id.equals("protection_pixel:lanceras_helmet"); }
    public static double bounded(double distance) { return !Double.isFinite(distance) || distance < 0 || distance > 20 ? 0 : Math.min(6, distance); }
    private static void write(ItemStack s, double value) { CustomData.update(DataComponents.CUSTOM_DATA, s, n -> n.putDouble("damage", value)); }
    public static void clamp(ItemStack s) { if (lancer(s)) write(s, bounded(s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("damage"))); }
    public static void forgetIfUnequipped(Player p) { if (!lancer(p.getItemBySlot(EquipmentSlot.HEAD))) SAMPLES.remove(p); }
    public static void sample(Entity entity, ItemStack helmet) {
        if (!(entity instanceof Player p) || p.level().isClientSide()) return;
        String dim = p.level().dimension().location().toString();
        Sample old = SAMPLES.get(p);
        if (old == null || old.helmet != helmet || !old.dimension.equals(dim) || p.tickCount - old.tick > 10 || p.tickCount < old.tick) {
            write(helmet, 0); SAMPLES.put(p, new Sample(helmet, dim, p.tickCount, p.position())); return;
        }
        if (p.tickCount - old.tick == 10) {
            write(helmet, bounded(p.position().distanceTo(old.position)));
            SAMPLES.put(p, new Sample(helmet, dim, p.tickCount, p.position()));
        }
    }
}
