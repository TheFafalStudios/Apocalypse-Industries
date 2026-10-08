// Apocalypse Industries - alternative Levitite Blend recipe
// Place in kubejs/server_scripts/ (server-side).
// Does not remove or modify Aeronautics' original recipe.

ServerEvents.recipes(event => {
  event.custom({
    type: 'create:mixing',
    heat_requirement: 'heated',
    ingredients: [
      { item: 'ae2:sky_dust' },
      { item: 'ae2:sky_dust' },
      { item: 'ae2:sky_dust' },
      { item: 'ae2:sky_dust' },
      { item: 'create:zinc_nugget' },
      { item: 'create:zinc_nugget' },
      {
        type: 'neoforge:single',
        amount: 1000,
        fluid: 'create_enchantment_industry:experience'
      }
    ],
    results: [
      { id: 'aeronautics:levitite_blend', amount: 500 }
    ]
  }).id('kubejs:levitite_blend_sky_stone_xp');
});