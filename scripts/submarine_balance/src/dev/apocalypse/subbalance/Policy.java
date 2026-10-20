package dev.apocalypse.subbalance;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Immutable pack policy, separate from Minecraft so boundary math is executable offline. */
public final class Policy {
    public static final Map<String,String> FAMILIES;
    static {
        var stream = Objects.requireNonNull(Policy.class.getResourceAsStream("/hull-families.json"));
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var json = new Gson().fromJson(reader, com.google.gson.JsonObject.class);
            Map<String,String> values = new HashMap<>();
            json.entrySet().forEach(e -> e.getValue().getAsJsonArray().forEach(id -> values.put(id.getAsString(), e.getKey())));
            FAMILIES = Map.copyOf(values);
        } catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }
    public static boolean namedWindow(String id) {
        String name = id.substring(id.indexOf(':') + 1);
        return name.contains("glass") || name.contains("window") || name.contains("porthole")
            || name.endsWith("_pane") || id.equals("create_submarine:iron_pressurizer")
            || id.equals("create_submarine:copper_pressurizer");
    }
    public static int cap(String id, int original, boolean lava, boolean window, boolean copied) {
        String family = FAMILIES.getOrDefault(id, "fallback");
        if (window || namedWindow(id)) return Math.min(original, lava ? 40 : 60);
        int cap = switch (family) {
            case "metal" -> lava ? 80 : 100;
            case "steel" -> lava ? 120 : 150;
            case "netherite_decoration" -> lava ? 145 : 150;
            case "solid_netherite" -> lava ? 170 : 225;
            default -> lava ? (id.startsWith("create_submarine:") ? 120 : 80) : Integer.MAX_VALUE;
        };
        // A copied decoration is not the full nine-ingot pressure block.
        if (copied) cap = Math.min(cap, lava ? 145 : 150);
        return Math.min(original, cap);
    }
}
