// Biological Recovery event items.
// Textures are supplied under assets/kubejs/textures/item with matching IDs.

StartupEvents.registry('item', event => {
    event.create('sealed_biocontainment_canister')
        .unstackable()
        .rarity('rare')
        .displayName('§6Sealed Biocontainment Canister')
        .tooltip('§7PRE-COLLAPSE BIOCONTAINMENT VESSEL')
        .tooltip('§6BIOLOGICAL HAZARD — DO NOT OPEN')
        .tooltip('§8Locking collar: INTACT')

    event.create('compromised_biocontainment_canister')
        .unstackable()
        .rarity('epic')
        .displayName('§cCompromised Biocontainment Canister')
        .tooltip('§cPRESSURE SEAL COMPROMISED')
        .tooltip('§7The Mechanical Press has deformed the locking collar.')
        .tooltip('§eRight-click in the air to remove the cap.')

    event.create('empty_biocontainment_canister')
        .unstackable()
        .displayName('§7Empty Biocontainment Canister')
        .tooltip('§8Containment integrity: 0%')

    event.create('extracted_biological_sample')
        .unstackable()
        .glow(true)
        .rarity('epic')
        .displayName('§aExtracted Biological Sample')
        .tooltip('§7Recovered pre-collapse biological material.')
        .tooltip('§6A private client is paying extremely well for this.')
})
