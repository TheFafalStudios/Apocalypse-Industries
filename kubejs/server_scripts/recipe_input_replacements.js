const recipeInputReplacements = [
    [{ output: 'ae2:pattern_provider' }, 'minecraft:iron_ingot', Ingredient.of('immersiveengineering:ingot_aluminum')],
    [{ output: 'ae2:molecular_assembler' }, 'minecraft:iron_ingot', Ingredient.of('immersiveengineering:ingot_aluminum')],
    [{ output: 'ae2:molecular_assembler' }, 'minecraft:crafting_table', Ingredient.of('create:mechanical_crafter')],
    [{ output: 'create:andesite_casing' }, '#c:stripped_logs', Ingredient.of('#immersiveengineering:treated_wood')],
    [{ output: 'create:copper_casing' }, '#c:stripped_logs', Ingredient.of('#immersiveengineering:treated_wood')],
    [{ output: 'create:brass_casing' }, '#c:stripped_logs', Ingredient.of('create:andesite_casing')],
    [{ output: 'immersiveengineering:component_steel' }, 'minecraft:copper_ingot', 'immersiveengineering:ingot_aluminum'],
    [{ output: 'immersiveengineering:component_iron' }, 'minecraft:copper_ingot', 'immersiveengineering:ingot_aluminum'],
    [{ output: 'immersiveengineering:conveyor_basic' }, 'minecraft:iron_ingot', 'immersiveengineering:ingot_aluminum'],
    [{ output: 'immersiveengineering:craftingtable' }, 'immersiveengineering:stick_treated', 'immersiveengineering:stick_aluminum'],
    [{ output: 'immersiveengineering:thermoelectric_generator' }, 'immersiveengineering:ingot_steel', 'immersiveengineering:ingot_aluminum'],
    [{ output: 'simulated:red_portable_engine' }, 'create:iron_sheet', 'immersiveengineering:plate_steel'],
    [{ output: 'create_rns:miner_bearing' }, 'create:shaft', 'createbigcannons:steel_screw_lock'],
        [{ output: 'create_rns:mine_head' }, 'minecraft:iron_ingot', 'immersiveengineering:ingot_steel'],
    [{ output: 'powergrid:electromagnet' }, 'powergrid:conductive_casing', 'powergrid:battery'],
    [{ output: 'creatingspace:rocket_controls' }, 'create:redstone_link', 'powergrid:constant_speed_motor'],
    [{ output: 'creatingspace:mechanical_electrolyzer' }, 'create:golden_sheet', 'powergrid:magnet'],
    [{ output: 'modulargolems:metal_golem_template' }, 'minecraft:clay_ball', 'immersiveengineering:coal_coke'],
    [{ output: 'modulargolems:empty_upgrade' }, 'minecraft:clay_ball', 'immersiveengineering:coal_coke'],
    [{ output: 'create:electron_tube' }, 'create:iron_sheet', 'immersiveengineering:plate_steel'],
     [{ output: 'ae2:energy_acceptor' }, 'minecraft:iron_ingot', 'powergrid:magnet']

]

ServerEvents.recipes(event => {
    recipeInputReplacements.forEach(([filter, input, replacement]) => {
        event.replaceInput(filter, input, replacement)
    })
})
