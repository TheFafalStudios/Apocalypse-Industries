// Steel is the highest recoverable zombie armor tier. Worn equipment is unchanged.
// Apply at death, including existing zombies and drowned converted with their armor.
const ApocalypseZombieArmorItem = Java.loadClass('net.minecraft.world.item.ArmorItem')
const apocalypseZombieAllowedArmor = new Set([
    'minecraft:turtle_helmet',
    'create:copper_diving_helmet',
    'create:copper_diving_boots',
    'createfisheryindustry:copper_diving_leggings'
])
for (const piece of ['helmet', 'chestplate', 'leggings', 'boots']) {
    for (const material of ['leather', 'golden', 'chainmail', 'iron']) {
        apocalypseZombieAllowedArmor.add('minecraft:' + material + '_' + piece)
    }
    apocalypseZombieAllowedArmor.add('create:cardboard_' + piece)
    apocalypseZombieAllowedArmor.add('immersiveengineering:armor_faraday_' + piece)
    apocalypseZombieAllowedArmor.add('immersiveengineering:armor_steel_' + piece)
}

function apocalypseFilterZombieArmorDrops(event) {
    event.getDrops().removeIf(drop => {
        const stack = drop.getItem()
        const isArmor = stack.getItem() instanceof ApocalypseZombieArmorItem ||
            stack.hasTag('minecraft:head_armor') || stack.hasTag('minecraft:chest_armor') ||
            stack.hasTag('minecraft:leg_armor') || stack.hasTag('minecraft:foot_armor')
        return isArmor && !apocalypseZombieAllowedArmor.has(String(stack.id))
    })
}

// Conversion transfers equipment and drop chances; filter the resulting drowned too.
EntityEvents.drops('minecraft:zombie', apocalypseFilterZombieArmorDrops)
EntityEvents.drops('minecraft:drowned', apocalypseFilterZombieArmorDrops)
