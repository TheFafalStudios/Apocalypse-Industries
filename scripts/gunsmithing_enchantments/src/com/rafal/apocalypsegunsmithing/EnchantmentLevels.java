package com.rafal.apocalypsegunsmithing;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;

/**
 * Looks up 1.21.1 data-driven enchantments without linking this compatibility
 * addon to Minecraft registry internals at compile time.  This also deliberately
 * does not clamp to an enchantment's declared max level, so CEI Super Enchanting
 * levels (normal max + 1) keep their stronger effects.
 */
public final class EnchantmentLevels {
    public static final String QUICK_HANDS = "apocalypse_gunsmithing:quick_hands";
    public static final String TRIGGER_FINGER = "apocalypse_gunsmithing:trigger_finger";
    public static final String LIGHTWEIGHT = "apocalypse_gunsmithing:lightweight";
    public static final String SHARPSHOOTER = "apocalypse_gunsmithing:sharpshooter";

    private static volatile Method getEnchantmentsForCrafting;
    private static volatile Method entrySet;
    private static volatile Method holderUnwrapKey;
    private static volatile Method resourceKeyLocation;
    private static volatile boolean failed;

    private EnchantmentLevels() {}

    public static int get(Object itemStack, String enchantmentId) {
        if (itemStack == null || enchantmentId == null) return 0;
        try {
            initialize(itemStack.getClass());
            Object enchantments = getEnchantmentsForCrafting.invoke(null, itemStack);
            if (enchantments == null) return 0;
            Object rawEntries = entrySet.invoke(enchantments);
            if (!(rawEntries instanceof Iterable<?> entries)) return 0;

            for (Object rawEntry : entries) {
                if (!(rawEntry instanceof Map.Entry<?, ?> entry)) continue;
                Object holder = entry.getKey();
                if (!enchantmentId.equals(holderId(holder))) continue;
                Object rawLevel = entry.getValue();
                return rawLevel instanceof Number number ? number.intValue() : 0;
            }
        } catch (Throwable error) {
            reportOnce(error);
        }
        return 0;
    }

    private static void initialize(Class<?> itemStackClass) throws ReflectiveOperationException {
        if (getEnchantmentsForCrafting != null) return;
        synchronized (EnchantmentLevels.class) {
            if (getEnchantmentsForCrafting != null) return;

            Class<?> helper = Class.forName("net.minecraft.world.item.enchantment.EnchantmentHelper");
            Method found = null;
            for (Method method : helper.getMethods()) {
                if (!method.getName().equals("getEnchantmentsForCrafting")) continue;
                if (!Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 1) continue;
                if (method.getParameterTypes()[0].isAssignableFrom(itemStackClass)) {
                    found = method;
                    break;
                }
            }
            if (found == null) throw new NoSuchMethodException("EnchantmentHelper.getEnchantmentsForCrafting(ItemStack)");

            Class<?> itemEnchantments = Class.forName("net.minecraft.world.item.enchantment.ItemEnchantments");
            Class<?> holder = Class.forName("net.minecraft.core.Holder");
            Class<?> resourceKey = Class.forName("net.minecraft.resources.ResourceKey");

            getEnchantmentsForCrafting = found;
            entrySet = itemEnchantments.getMethod("entrySet");
            holderUnwrapKey = holder.getMethod("unwrapKey");
            resourceKeyLocation = resourceKey.getMethod("location");
        }
    }

    private static String holderId(Object holder) throws ReflectiveOperationException {
        if (holder == null) return null;
        Object rawOptional = holderUnwrapKey.invoke(holder);
        if (!(rawOptional instanceof Optional<?> optional) || optional.isEmpty()) return null;
        Object key = optional.get();
        Object location = resourceKeyLocation.invoke(key);
        return String.valueOf(location);
    }

    private static void reportOnce(Throwable error) {
        if (failed) return;
        failed = true;
        System.err.println("[Apocalypse Gunsmithing] Failed to read gun enchantment levels; enchantment effects are disabled.");
        error.printStackTrace();
    }
}
