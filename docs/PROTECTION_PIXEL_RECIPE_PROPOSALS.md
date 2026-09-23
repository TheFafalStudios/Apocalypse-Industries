# Protection Pixel approved recipe changes

Date: 2026-09-20

## User scope and pending review
The user requested precise proposals for prior review points 3, 4, 5, 6, 7 and 9 FIRST. The user subsequently approved all nine listed recipe changes. They were implemented on 2026-09-20. This approval does not extend to the deferred points below.

Defer the other review points until a later detailed review: (1) heat-overlocking mechanism, (2) duplicated Precision Mechanism charges, (8) Tosaki ceramics, (10) consumables, (11) Echo Plate recipe, and (12) recipe correctness/alternate access. Keep these outstanding; do not silently implement or discard them. No quest, texture, runtime balance, server, release or Packwiz changes are part of this proposal.

## Assumptions and potential problems to retain
- Proposed recipes preserve existing item effects and output counts. Batteries and motors are consumed manufacturing inputs; they do not implement FE-powered equipment.
- Control chip native assembly consumes one golden sheet, three electron tubes and three redstone per attempt; native weights total 150, with 120 for the chip (80% success). This is a real recurring production cost, so use chips selectively.
- A Powergrid constant-speed motor consumes one electric motor plus one Precision Mechanism. Each electric motor consumes four iron plates, four copper coils, two magnets, one shaft and one conductive casing. Two motors therefore embed two Precision Mechanisms, eight iron plates, eight coils, four magnets, two shafts and two casings.
- Battery native assembly consumes a conductive casing, three copper plates, three zinc plates and 750 mB acid per attempt. Native success weight is 100/109, approximately 91.74%. Expected gross acid consumption is 817.5 mB per battery. Upstream recipes are not changed here.
- Float Shield electrum links to the existing heated gold+silver mixing recipe, which outputs two electrum. Other retained electrum production routes remain valid.
- Brass-age equipment must not acquire an accidental electricity requirement. Native control-chip production uses Create assembly; effective upstream ingredient replacements must still be inspected before implementation acceptance.
- Shops and player trade are legitimate material access. These recipes do not establish personal progression locks.
- Keep inexpensive Iron/Brass plates and Link Plate recipes unchanged under point 3. Defer shared heat mechanism to point 1.
- Keep other external devices unchanged under point 6: oxygen gear, thrusters, hook gear, launch arms and ordinary wings. No blanket motors, steel or insulating glass substitution.
- Native runtime loading, absence of alternate bypass recipes, ingredient tags, and actual crafting are not tested by this proposal. Confirm after approval. No claim of combat balance validation.
- Implement approved exact-ID overrides in the recipe generator as well as generated outputs so regeneration cannot undo them. Preserve other current pack edits. The new hemp route is intentionally additional; modified existing recipes must not leave their cheaper original alongside them.

## Exact proposed recipes
The JSON below records the approved recipes and matches the installed datapack content. Existing crafting layouts are retained except the specified removal of two dialysis tubes and the hanger center. All listed final crafting outputs are deterministic; upstream chip/battery failure chances are separate.

### Point 3a - protection_pixel:alloyplate

Replace one iron sheet with one IE steel plate. Preserve one loop, deterministic output and every other ingredient.

```json
{
  "type": "create:sequenced_assembly",
  "ingredient": {
    "item": "protection_pixel:smallnetheritesheet"
  },
  "loops": 1,
  "results": [
    {
      "id": "protection_pixel:alloyarmorplate"
    }
  ],
  "sequence": [
    {
      "type": "create:pressing",
      "ingredients": [
        {
          "item": "protection_pixel:incompletealloyarmorplate"
        }
      ],
      "results": [
        {
          "id": "protection_pixel:incompletealloyarmorplate"
        }
      ]
    },
    {
      "type": "create:deploying",
      "ingredients": [
        {
          "item": "protection_pixel:incompletealloyarmorplate"
        },
        {
          "item": "immersiveengineering:plate_steel"
        }
      ],
      "results": [
        {
          "id": "protection_pixel:incompletealloyarmorplate"
        }
      ]
    },
    {
      "type": "create:deploying",
      "ingredients": [
        {
          "item": "protection_pixel:incompletealloyarmorplate"
        },
        {
          "item": "create:andesite_alloy"
        }
      ],
      "results": [
        {
          "id": "protection_pixel:incompletealloyarmorplate"
        }
      ]
    },
    {
      "type": "create:deploying",
      "ingredients": [
        {
          "item": "protection_pixel:incompletealloyarmorplate"
        },
        {
          "item": "powergrid:magnet"
        }
      ],
      "results": [
        {
          "id": "protection_pixel:incompletealloyarmorplate"
        }
      ]
    }
  ],
  "transitional_item": {
    "id": "protection_pixel:incompletealloyarmorplate"
  }
}
```

### Point 3b - protection_pixel:steamexoskeletonloot

Replace all four iron sheets with four IE steel plates. Keep steam engines, copper and the existing magnet.

```json
{
  "type": "create:mechanical_crafting",
  "accept_mirrored": true,
  "key": {
    "A": {
      "item": "create:cogwheel"
    },
    "B": {
      "item": "create:copper_sheet"
    },
    "C": {
      "item": "create:steam_engine"
    },
    "D": {
      "item": "minecraft:redstone"
    },
    "E": {
      "item": "immersiveengineering:plate_steel"
    },
    "G": {
      "item": "powergrid:magnet"
    }
  },
  "pattern": [
    "ABBBA",
    "CDEDC",
    " BEB ",
    "AE EA",
    "BB BB",
    "  G  "
  ],
  "result": {
    "id": "protection_pixel:steamectoskeleton",
    "count": 1
  }
}
```

### Point 4 - protection_pixel:cannonshellloot

Replace one iron ingot with one IE steel ingot. Preserve shaped layout and output of two. No additional ammunition route proposed in this first pass.

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " ab",
    "aca",
    "ca "
  ],
  "key": {
    "a": {
      "item": "create:andesite_alloy"
    },
    "b": {
      "item": "immersiveengineering:ingot_steel"
    },
    "c": {
      "item": "minecraft:gunpowder"
    }
  },
  "result": {
    "id": "protection_pixel:steelcorecannonshell",
    "count": 2
  }
}
```

### Point 5 - apocalypse_pp:reinforced_fiber_from_hemp

New alternative only; retain protection_pixel:fiberloot.

```json
{
  "type": "minecraft:crafting_shapeless",
  "ingredients": [
    {
      "item": "immersiveengineering:hemp_fabric"
    },
    {
      "item": "minecraft:quartz"
    },
    {
      "item": "minecraft:quartz"
    }
  ],
  "result": {
    "id": "protection_pixel:reinforcedfiber",
    "count": 2
  }
}
```

### Point 6a - protection_pixel:hunterloot

Replace the added Precision Mechanism with one control chip. Keep the existing electron tube and all optical materials. No electricity ingredient introduced by this substitution.

```json
{
  "type": "create:mechanical_crafting",
  "accept_mirrored": true,
  "category": "misc",
  "key": {
    "A": {
      "tag": "c:ingots/brass"
    },
    "B": {
      "item": "minecraft:amethyst_shard"
    },
    "C": {
      "item": "create:brass_sheet"
    },
    "D": {
      "item": "create:electron_tube"
    },
    "E": {
      "item": "minecraft:iron_helmet"
    },
    "F": {
      "item": "create:iron_sheet"
    },
    "G": {
      "item": "create_connected:control_chip"
    }
  },
  "pattern": [
    "AB BA",
    " ACA ",
    " DEC ",
    " FAF ",
    "  G  "
  ],
  "result": {
    "id": "protection_pixel:hunter_helmet",
    "count": 1
  }
}
```

### Point 6b - protection_pixel:blooddialysisdeviceloot

Replace Precision Mechanism with one control chip and remove the two upper corner electron tubes. Leaves two direct electron tubes, four iron sheets, four tanks, one whisk and two pumps.

```json
{
  "type": "create:mechanical_crafting",
  "accept_mirrored": true,
  "category": "misc",
  "key": {
    "A": {
      "item": "create:electron_tube"
    },
    "B": {
      "item": "create:iron_sheet"
    },
    "C": {
      "item": "create_connected:control_chip"
    },
    "D": {
      "item": "create:fluid_tank"
    },
    "E": {
      "item": "create:whisk"
    },
    "F": {
      "item": "create:mechanical_pump"
    }
  },
  "pattern": [
    " BCB ",
    "ADEDA",
    "DF FD",
    " B B "
  ],
  "result": {
    "id": "protection_pixel:blooddialysisdevice",
    "count": 1
  }
}
```

### Point 7a - protection_pixel:floatshieldloot

Replace direct Precision Mechanism with one battery and two copper ingots with two electrum ingots. Lining still embeds a Precision Mechanism. Preserve lunar gate and Heart of the Sea. Crafting ingredient only: does not add FE-powered shield mechanics.

```json
{
  "type": "create:mechanical_crafting",
  "accept_mirrored": true,
  "category": "misc",
  "key": {
    "A": {
      "item": "minecraft:lightning_rod"
    },
    "B": {
      "item": "create:sturdy_sheet"
    },
    "C": {
      "item": "immersiveengineering:ingot_electrum"
    },
    "D": {
      "item": "create:copper_sheet"
    },
    "E": {
      "item": "create:andesite_alloy"
    },
    "F": {
      "item": "protection_pixel:chestplatelining"
    },
    "H": {
      "item": "minecraft:heart_of_the_sea"
    },
    "I": {
      "item": "minecraft:netherite_scrap"
    },
    "J": {
      "item": "powergrid:battery"
    },
    "G": {
      "item": "create:cogwheel"
    },
    "K": {
      "item": "creatingspace:moon_regolith"
    }
  },
  "pattern": [
    "AB BA",
    "BCBCB",
    "DEFED",
    "BGHGB",
    " IJI ",
    "  K  "
  ],
  "result": {
    "id": "protection_pixel:floatshield_chestplate",
    "count": 1
  }
}
```

### Point 7b - protection_pixel:evasionwingloot

Replace both Precision Mechanisms with two constant-speed motors. Each motor already consumes one Precision Mechanism; preserve all other ingredients and lunar gate. No change to runtime power behavior.

```json
{
  "type": "create:mechanical_crafting",
  "accept_mirrored": true,
  "category": "misc",
  "key": {
    "A": {
      "item": "create:iron_sheet"
    },
    "B": {
      "item": "minecraft:iron_ingot"
    },
    "C": {
      "item": "create:andesite_alloy"
    },
    "D": {
      "item": "powergrid:constant_speed_motor"
    },
    "E": {
      "item": "create:cogwheel"
    },
    "F": {
      "item": "create:transmitter"
    },
    "G": {
      "item": "creatingspace:moon_regolith"
    }
  },
  "pattern": [
    "AB BA",
    "ACACA",
    "DEFED",
    "A   A",
    "  G  "
  ],
  "result": {
    "id": "protection_pixel:evasionwing",
    "count": 1
  }
}
```

### Point 9 - protection_pixel:armorhangerloot

Restore native hanger recipe: remove the added Precision Mechanism, preserve output two and all other ingredients.

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "aba",
    "c c",
    "dad"
  ],
  "key": {
    "a": {
      "item": "minecraft:iron_ingot"
    },
    "b": {
      "item": "minecraft:shroomlight"
    },
    "c": {
      "item": "minecraft:chain"
    },
    "d": {
      "item": "minecraft:iron_nugget"
    }
  },
  "result": {
    "id": "protection_pixel:armorhanger",
    "count": 2
  }
}
```

## Implementation validation - 2026-09-20
- All nine installed recipe definitions match the approved JSON exactly.
- 445 structural assertions pass. Regenerating twice preserves all recipe hashes.
- Isolated dedicated server loaded all nine recipes and retained the original fiber recipe. Native serialization confirms ingredient quantities, crafting patterns, outputs and Alloy assembly steps after material unification. Steel/electrum and other metals are converted to shared material tags by Almost Unified; no quantities change.
- Only the nine approved recipe manifest entries and the index checksum were refreshed. No push, release or Chunkserve twin update.
- No manual in-game crafting or combat balance test is claimed. Deferred review points remain pending.
