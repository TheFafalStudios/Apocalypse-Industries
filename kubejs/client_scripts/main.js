// Visit the wiki for more info - https://kubejs.com/
console.info('Hello, World! (Loaded client example script)')

RecipeViewerEvents.addInformation('item', event => {
    event.add('biomesoplenty:dried_salt', [
        'Dried salt can be excavated by right clicking with a shovel.',
        'This has a chance to drop dirt, sand, or clay.'
    ])
})