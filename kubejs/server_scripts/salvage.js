ServerEvents.recipes(event => {
    const blastNuggets = [
        ['minecraft:iron_nugget', 'minecraft:iron_bars'],
        ['minecraft:iron_nugget', 'minecraft:iron_trapdoor'],
        ['create:copper_nugget', 'minecraft:copper_block'],
        ['minecraft:iron_nugget', 'minecraft:rail'],
        ['minecraft:iron_nugget', 'minecraft:lantern'],
        [Item.of('create:zinc_nugget', 3), 'kubejs:old_world_tech'],
        ['minecraft:gold_nugget', 'kubejs:scrap_electronics'],
        ['minecraft:iron_nugget', 'kubejs:rusted_metal_scrap'],
        ['create:zinc_nugget', 'minecraft:chain']
    ]

    blastNuggets.forEach(([output, input]) => {
        event.blasting(output, input)
    })

    const scrap = 'kubejs:rusted_metal_scrap'
    const stick = 'minecraft:stick'

    event.shaped('kubejs:scrap_sword', [
        'M',
        'M',
        'S'
    ], {
        M: scrap,
        S: stick
    })

    event.shaped('kubejs:scrap_pickaxe', [
        'MMM',
        ' S ',
        ' S '
    ], {
        M: scrap,
        S: stick
    })

    event.shaped('kubejs:scrap_axe', [
        'MM',
        'MS',
        ' S'
    ], {
        M: scrap,
        S: stick
    })

    event.shaped('kubejs:scrap_shovel', [
        'M',
        'S',
        'S'
    ], {
        M: scrap,
        S: stick
    })

    event.shaped('kubejs:scrap_hoe', [
        'MM',
        ' S',
        ' S'
    ], {
        M: scrap,
        S: stick
    })

    event.recipes.create.crushing([
        CreateItem.of('minecraft:diamond', 0.025),
        CreateItem.of('immersiveengineering:ingot_steel', 0.05),
        CreateItem.of('create:brass_ingot', 0.05),
        CreateItem.of('kubejs:rusted_metal_scrap', 0.25),
        CreateItem.of('kubejs:scrap_electronics', 0.25)
  ], 'kubejs:dense_scrap')
})

const salvage = [
    'kubejs:scrap_electronics',
    'kubejs:old_world_tech',
    'kubejs:pristine_old_world_tech',
    'kubejs:worn_book',
    'kubejs:old_world_encyclopedia',
    'kubejs:archaic_collectable',
    'kubejs:faded_photo',
    'kubejs:rusted_metal_scrap',
    'kubejs:pristine_ancient_relic',
    'kubejs:antique_canned_food'
]

ServerEvents.tags('item', event => {
    salvage.forEach(id => {
        event.add('kubejs:salvage', id)
    })
})


