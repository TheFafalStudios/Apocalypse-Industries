package com.rafal.apocalypsegunsmithing;

public final class GunTypes {
    private static final String CGS_GUN_PACKAGE = "com.nukateam.cgs.common.faundation.item.guns.";

    private GunTypes() {}

    public static boolean isEnchantableCgsGun(Object item) {
        if (item == null) return false;
        String name = item.getClass().getName();
        if (!name.startsWith(CGS_GUN_PACKAGE)) return false;
        // HammerItem shares CgsGunItem, but is a pneumatic melee tool rather than a gun.
        return !name.equals(CGS_GUN_PACKAGE + "HammerItem")
                && !name.equals(CGS_GUN_PACKAGE + "CgsGunItem");
    }
}
