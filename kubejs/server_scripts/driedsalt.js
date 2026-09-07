BlockEvents.rightClicked(event => {
    const { player, block, level, item, hand, server } = event;

    if (hand !== 'MAIN_HAND') return;
    if (block.id !== 'biomesoplenty:dried_salt') return;
    if (!item.hasTag('minecraft:shovels')) return;

    const roll = Math.random();
    let drop = null;

    if (roll < 0.7) {
        drop = null;
    } else if (roll < 0.85) {
        drop = 'minecraft:dirt';
    } else if (roll < 0.95) {
        drop = 'minecraft:sand';
    } else {
        drop = 'minecraft:clay_ball';
    }

    if (drop !== null) {
        block.popItem(drop);
    }

    if (!player.isCreative()) item.setDamageValue(item.getDamageValue() + 5);
    if (item.getDamageValue() >= item.getMaxDamage()) item.setCount(0);

    block.set('minecraft:air');

    server.runCommandSilent(`playsound minecraft:block.gravel.break block @a ${block.x} ${block.y} ${block.z} 1 1`);

    level.spawnParticles(
        'minecraft:smoke',
        true,
        block.x + 0.5,
        block.y + 0.6,
        block.z + 0.5,
        0.25,
        0.15,
        0.25,
        12,
        0.03
    );

    event.cancel();
});