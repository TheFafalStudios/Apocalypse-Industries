ServerEvents.recipes(event => {
  

   


    event.recipes.create.splashing([CreateItem.of('immersiveengineering:nugget_silver', 0.25)], 'create:powdered_obsidian')
    event.recipes.create.splashing(CreateItem.of('create:zinc_nugget', 0.15), 'ratatouille:compost_mass')
    event.recipes.create.crushing([CreateItem.of('immersiveengineering:nugget_lead', 0.15)], 'minecraft:blackstone')
    event.recipes.create.splashing([CreateItem.of('create:copper_nugget', 0.15)], 'createfisheryindustry:fish_skin')

    event.recipes.create.splashing([
        CreateItem.of('create:zinc_nugget', 0.02),
        CreateItem.of('immersiveengineering:nugget_lead', 0.02),
        CreateItem.of('immersiveengineering:nugget_silver', 0.02)
    ], 'biomesoplenty:dried_salt')

    event.recipes.create.mixing([
  CreateItem.of('immersiveengineering:nugget_silver', 0.5)
], 'create:powdered_obsidian').superheated()

event.recipes.create.mixing([
  CreateItem.of('create:zinc_nugget', 0.3)
], 'ratatouille:compost_mass').superheated()

event.recipes.create.mixing([
  CreateItem.of('immersiveengineering:nugget_lead', 0.3)
], 'minecraft:blackstone').superheated()

event.recipes.create.mixing([
  CreateItem.of('create:copper_nugget', 0.3)
], 'createfisheryindustry:fish_skin').superheated()

event.recipes.create.mixing([
  CreateItem.of('create:zinc_nugget', 0.04),
  CreateItem.of('immersiveengineering:nugget_lead', 0.04),
  CreateItem.of('immersiveengineering:nugget_silver', 0.04)
], 'biomesoplenty:dried_salt').superheated()

event.recipes.create.mixing([
  CreateItem.of('minecraft:iron_nugget', 0.25)
], 'minecraft:gravel').superheated()

event.recipes.create.mixing([
  CreateItem.of('minecraft:gold_nugget', 0.06)
], 'minecraft:soul_sand').superheated()

    event.recipes.create.mixing(['2x immersiveengineering:ingot_constantan'], [
        Ingredient.of('minecraft:copper_ingot'),
        Ingredient.of('immersiveengineering:ingot_nickel')
    ]).heated()

    event.recipes.create.mixing(['2x immersiveengineering:ingot_electrum'], [
        Ingredient.of('minecraft:gold_ingot'),
        Ingredient.of('immersiveengineering:ingot_silver')
    ]).heated()

    event.recipes.create.mixing(['2x immersiveengineering:insulating_glass'], [
        Ingredient.of('#createbigcannons:glass', 2),
        Ingredient.of('immersiveengineering:dust_iron')
    ]).heated()

    event.recipes.create.mixing(['biomesoplenty:dried_salt'], [
        Ingredient.of('ratatouille:salt', 4),
        Ingredient.of('minecraft:sand')
    ]).heated()

    event.recipes.create.mixing([CreateItem.of('minecraft:coal', 0.5)], [
        Fluid.of('immersiveengineering:creosote', 250),
        Ingredient.of('biomesoplenty:dead_wood')
    ]).heated()

    event.recipes.create.deploying(Item.of('immersiveengineering:empty_casing', 2), [
        'minecraft:copper_ingot',
        'immersiveengineering:mold_bullet_casing'
    ]).keepHeldItem()

    event.recipes.create.deploying(Item.of('immersiveengineering:stick_aluminum', 2), [
        'immersiveengineering:ingot_aluminum',
        'immersiveengineering:mold_rod'
    ]).keepHeldItem()

    event.recipes.create.deploying(Item.of('immersiveengineering:stick_steel', 2), [
        'immersiveengineering:ingot_steel',
        'immersiveengineering:mold_rod'
    ]).keepHeldItem()

    event.recipes.create.deploying(Item.of('immersiveengineering:stick_netherite', 2), [
        'minecraft:netherite_ingot',
        'immersiveengineering:mold_rod'
    ]).keepHeldItem()

    event.recipes.create.deploying(Item.of('immersiveengineering:stick_iron', 2), [
        'minecraft:iron_ingot',
        'immersiveengineering:mold_rod'
    ]).keepHeldItem()
})
