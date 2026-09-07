const deadLogs = [
    'biomesoplenty:dead_log',
    'biomesoplenty:dead_wood',
    'biomesoplenty:stripped_dead_log',
    'biomesoplenty:stripped_dead_wood'
]

ServerEvents.tags('block', event => {
    deadLogs.forEach(id => {
        event.removeAllTagsFrom(id)
        event.add('kubejs:dead_logs', id)
    })

    event.remove('minecraft:planks', 'biomesoplenty:dead_planks')
})

ServerEvents.tags('item', event => {
    deadLogs.forEach(id => {
        event.removeAllTagsFrom(id)
        event.add('kubejs:dead_logs', id)
    })

    event.remove('minecraft:planks', 'biomesoplenty:dead_planks')
})


ServerEvents.recipes(event => {
    const deadPlanks = 'biomesoplenty:dead_planks'
    const stick = 'minecraft:stick'
    const damage = 45

    const damaged = id => Item.of(`${id}[minecraft:damage=${damage}]`)

    event.shaped(damaged('minecraft:crafting_table'), [
        'PP',
        'PP',
    ], {
        P: deadPlanks
    })

       event.shaped(damaged('minecraft:chest'), [
        'PPP',
        'P P',
        'PPP'
    ], {
        P: deadPlanks
    })

     event.shaped(
        Item.of('minecraft:stick', 2),
        [
            'P',
            'P'
        ],
        {
            P: deadPlanks
        }
    )

      event.shapeless(Item.of('biomesoplenty:dead_planks', 4), [
        '#kubejs:dead_logs'
    ])
})