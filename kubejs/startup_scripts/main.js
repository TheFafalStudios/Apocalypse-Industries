console.info('Hello, World! (Loaded startup example script)')

Platform.mods.kubejs.name = 'Apocalypse Industries'

StartupEvents.registry('item', event => {
    event.create('scrap_electronics')
    event.create('old_world_tech')
    event.create('worn_book')
    event.create('old_world_encyclopedia')
    event.create('archaic_collectable')
    event.create('faded_photo')
    event.create('rusted_metal_scrap')

    event.create('pristine_ancient_relic')
        .glow(true)
        .rarity('epic')
        .displayName('§6Pristine Ancient Relic')

    event.create('pristine_old_world_tech')
        .glow(true)
        .rarity('epic')
        .displayName('§6Pristine Old World Tech')

    event.create('antique_canned_food').food(food => {
        food.nutrition(4)
        food.saturation(4)
        food.effect('minecraft:poison', 60, 0, 1)
    })

    event.create('nether_pass').displayName('Nether Authorisation Document')

    event.create('infection_cure').unstackable().food(food => {
        food.nutrition(0.5)
        food.saturation(0.5)
        food.alwaysEdible()
        food.fastToEat()
    })

     event.create('scrap_sword', 'sword').tier('stone').maxDamage(96).displayName('Scrap Sword')
    event.create('scrap_pickaxe', 'pickaxe').tier('stone').maxDamage(96).displayName('Scrap Pickaxe')
    event.create('scrap_axe', 'axe').tier('stone').maxDamage(96).displayName('Scrap Axe')
    event.create('scrap_shovel', 'shovel').tier('stone').maxDamage(96).displayName('Scrap Shovel')
    event.create('scrap_hoe', 'hoe').tier('stone').maxDamage(96).displayName('Scrap Hoe')
})

StartupEvents.registry('block', event => {
  event.create('dense_scrap')
    .displayName('Dense Scrap')
    .soundType('ancient_debris')
    .hardness(5)
    .resistance(6)
    .requiresTool(true)
    .tagBlock('minecraft:mineable/pickaxe')
    .tagBlock('minecraft:needs_diamond_tool')
})