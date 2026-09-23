const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const root = path.resolve(__dirname, '..')
class ArmorItem {}
const listeners = new Map()
vm.runInNewContext(fs.readFileSync(path.join(root, 'kubejs/server_scripts/apocalypse_zombie_armor_drops.js'), 'utf8'), {
    Java: { loadClass: name => { assert.equal(name, 'net.minecraft.world.item.ArmorItem'); return ArmorItem } },
    EntityEvents: { drops: (target, callback) => { assert.ok(!listeners.has(target)); listeners.set(target, callback) } }
})
assert.deepEqual([...listeners.keys()].sort(), ['minecraft:drowned', 'minecraft:zombie'])
function simulate(entries, target = 'minecraft:zombie') {
    const drops = entries.map(([id, armor, tagged = false]) => ({
        getItem: () => ({ id, getItem: () => armor ? new ArmorItem() : {}, hasTag: () => tagged })
    }))
    const listener = listeners.get(target)
    if (listener) listener({ getDrops: () => ({ removeIf: predicate => {
        for (let i = drops.length - 1; i >= 0; --i) if (predicate(drops[i])) drops.splice(i, 1)
    } }) })
    return drops.map(drop => drop.getItem().id)
}
const rules = JSON.parse(fs.readFileSync(path.join(root, 'config/incontrol/spawn.json'), 'utf8'))
let checked = 0
for (const rule of rules) {
    for (const slot of ['armorhelmet', 'armorchest', 'armorlegs', 'armorboots']) {
        for (const id of rule[slot] || []) {
            const blocked = /diamond_|echo_armor_|spacesuit_|netherite_diving_/.test(id)
            for (const target of listeners.keys()) {
                assert.deepEqual(simulate([[id, true]], target), blocked ? [] : [id], `${target} ${rule.phase}: ${id}`)
                checked++
            }
        }
    }
}
assert.deepEqual(simulate([
    ['minecraft:diamond_helmet', true], ['minecraft:rotten_flesh', false],
    ['create_deep_dark:echo_armor_boots', true], ['immersiveengineering:armor_steel_boots', true],
    ['minecraft:iron_ingot', false], ['minecraft:carrot', false], ['minecraft:potato', false],
    ['minecraft:diamond_sword', false], ['minecraft:netherite_chestplate', true],
    ['example:future_armor', true], ['example:tagged_armor', false, true]
]), ['minecraft:rotten_flesh', 'immersiveengineering:armor_steel_boots', 'minecraft:iron_ingot',
    'minecraft:carrot', 'minecraft:potato', 'minecraft:diamond_sword'])
assert.deepEqual(simulate([]), [])
// Model the equipment transfer observed in Mob.convertTo: no death event on the
// original zombie; the unchanged equipment reaches the drowned death handler.
const transferredArmor = [
    ['minecraft:diamond_helmet', true], ['create_deep_dark:echo_armor_chestplate', true],
    ['createfisheryindustry:netherite_diving_leggings', true],
    ['creatingspace:advanced_spacesuit_boots', true], ['immersiveengineering:armor_steel_boots', true]
]
const drownedLoot = [['minecraft:trident', false], ['minecraft:rotten_flesh', false],
    ['minecraft:copper_ingot', false], ['minecraft:nautilus_shell', false], ['minecraft:fishing_rod', false]]
assert.deepEqual(simulate([...transferredArmor, ...drownedLoot], 'minecraft:drowned'),
    ['immersiveengineering:armor_steel_boots', ...drownedLoot.map(entry => entry[0])])
assert.deepEqual(simulate(transferredArmor, 'minecraft:skeleton'), transferredArmor.map(entry => entry[0]))
assert.deepEqual(simulate(transferredArmor, 'minecraft:player'), transferredArmor.map(entry => entry[0]))
assert.deepEqual(simulate([], 'minecraft:drowned'), [])
console.log(`PASS: ${checked} progression armor cases across zombies and drowned; transferred armor capped; steel, tridents and normal loot preserved; other entities unaffected. Mocked API, not an in-game conversion test.`)
