// Create processing step for the Biological Recovery Special Request.
// Pressing does NOT release Spore; it only compromises the seal.
// The deliberate right-click opening action in spore_lock.js releases it.

ServerEvents.recipes(event => {
    event.recipes.create.pressing(
        'kubejs:compromised_biocontainment_canister',
        'kubejs:sealed_biocontainment_canister'
    ).id('kubejs:pressing/compromise_biocontainment_canister')
})
