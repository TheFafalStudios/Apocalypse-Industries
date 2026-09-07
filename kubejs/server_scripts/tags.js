ServerEvents.tags('item', event => {
    

    const cgsGuns = [
        'cgs:shotgun',
        'cgs:gatling',
        'cgs:nailgun',
        'cgs:blazegun',
        'cgs:launcher',
    ]

    cgsGuns.forEach(id => event.add('kubejs:cgs_guns', id))

    event.remove('c:ores/cobalt', 'creatingspace:crushed_cobalt_ore')

})