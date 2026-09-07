// Spore infection lock + Biological Recovery outbreak event.
// Minecraft 1.21.1 NeoForge / KubeJS 7.2 / In Control 10.2.6
//
// Compatibility design:
// - KubeJS server.persistentData stores the permanent world state.
// - "spore_locked" is a MANUAL In Control phase.
// - It is intentionally NOT defined in phases.json.
// - On every server/world load KubeJS reconciles the manual phase with the
//   persistent state. This avoids the unsupported kubejs keyword in the
//   installed In Control 10.2.6 phases.json parser.

const SPORE_LOCK_INITIALIZED = 'spore_lock_initialized'
const SPORE_LOCKED = 'spore_locked'
const SPORE_OUTBREAK_TRIGGERED = 'spore_outbreak_triggered'

function syncSporePhase(server) {
    if (server.persistentData.getBoolean(SPORE_LOCKED)) {
        server.runCommandSilent('incontrol setphase spore_locked')
    } else {
        server.runCommandSilent('incontrol clearphase spore_locked')
    }
}

function initializeSporeLock(server) {
    const data = server.persistentData

    // New worlds begin locked. Existing worlds retain their stored release state.
    if (!data.getBoolean(SPORE_LOCK_INITIALIZED)) {
        data.putBoolean(SPORE_LOCKED, true)
        data.putBoolean(SPORE_OUTBREAK_TRIGGERED, false)
        data.putBoolean(SPORE_LOCK_INITIALIZED, true)
        console.info('[Spore Lock] Initialized this world with Spore locked.')
    }

    // Reconcile In Control every load. This also makes the design independent of
    // whether manually-set In Control phases themselves persist across restarts.
    syncSporePhase(server)
}

function setSporeReleased(server) {
    const data = server.persistentData
    const firstOutbreak = data.getBoolean(SPORE_LOCKED) && !data.getBoolean(SPORE_OUTBREAK_TRIGGERED)

    data.putBoolean(SPORE_LOCKED, false)
    data.putBoolean(SPORE_OUTBREAK_TRIGGERED, true)
    server.runCommandSilent('incontrol clearphase spore_locked')

    return firstOutbreak
}

function setSporeLocked(server, rearmOutbreak) {
    const data = server.persistentData

    data.putBoolean(SPORE_LOCKED, true)
    if (rearmOutbreak) {
        data.putBoolean(SPORE_OUTBREAK_TRIGGERED, false)
    }

    server.runCommandSilent('incontrol setphase spore_locked')
}

function broadcastOutbreak(server, player) {
    const x = Math.floor(player.x)
    const y = Math.floor(player.y)
    const z = Math.floor(player.z)

    server.runCommandSilent('title @a times 10 70 20')
    server.runCommandSilent('title @a title {"text":"CONTAINMENT FAILURE","color":"dark_red","bold":true}')
    server.runCommandSilent('title @a subtitle {"text":"Unknown biological agent detected","color":"red"}')
    server.runCommandSilent('execute as @a at @s run playsound minecraft:block.beacon.deactivate master @s ~ ~ ~ 1 0.55')

    server.tell('§4[EMERGENCY BROADCAST] §cBIOLOGICAL CONTAINMENT FAILURE')
    server.tell(`§7Origin triangulated near §f${x}, ${y}, ${z}§7.`)

    server.scheduleInTicks(35, server, ctx => {
        const scheduledServer = ctx.data
        scheduledServer.runCommandSilent('execute as @a at @s run playsound minecraft:entity.warden.heartbeat master @s ~ ~ ~ 0.8 0.7')
        scheduledServer.tell('§6[EMERGENCY BROADCAST] §eUnknown airborne biological material detected.')
    })

    server.scheduleInTicks(75, server, ctx => {
        const scheduledServer = ctx.data
        scheduledServer.runCommandSilent('title @a times 5 50 15')
        scheduledServer.runCommandSilent('title @a title {"text":"QUARANTINE PROTOCOL FAILED","color":"red","bold":true}')
        scheduledServer.runCommandSilent('title @a subtitle {"text":"Atmospheric contamination: UNCONTROLLED","color":"dark_red"}')
        scheduledServer.runCommandSilent('execute as @a at @s run playsound minecraft:entity.warden.heartbeat master @s ~ ~ ~ 1 0.5')
    })
}

ServerEvents.loaded(event => {
    initializeSporeLock(event.server)
})

// Deliberate final step: the player manually removes the cap after pressing it.
ItemEvents.rightClicked('kubejs:compromised_biocontainment_canister', event => {
    const { player, item, level, server } = event

    if (!player || !server) return

    item.count--
    player.give('kubejs:empty_biocontainment_canister')
    player.give('kubejs:extracted_biological_sample')

    level.spawnParticles(
        'minecraft:spore_blossom_air',
        true,
        player.x,
        player.y + 1.0,
        player.z,
        0.65,
        0.45,
        0.65,
        80,
        0.03
    )
    level.spawnParticles(
        'minecraft:ash',
        true,
        player.x,
        player.y + 1.0,
        player.z,
        0.45,
        0.35,
        0.45,
        35,
        0.02
    )
    server.runCommandSilent(`playsound minecraft:block.brewing_stand.brew master @a ${player.x} ${player.y} ${player.z} 1 0.55`)

    const firstOutbreak = setSporeReleased(server)
    if (firstOutbreak) {
        player.tell('§8The pressure seal gives way with a wet hiss.')
        broadcastOutbreak(server, player)
        console.warn(`[Spore Lock] Infection released by opening a compromised canister at ${Math.floor(player.x)}, ${Math.floor(player.y)}, ${Math.floor(player.z)}.`)
    } else {
        player.tell('§7You recover another biological sample from the compromised vessel.')
    }

    event.cancel()
})

ServerEvents.commandRegistry(event => {
    const { commands: Commands } = event

    // Fallback/admin release. Does not replay the cinematic.
    event.register(
        Commands.literal('spore_release')
            .requires(source => source.hasPermission(2))
            .executes(ctx => {
                const server = ctx.source.getServer()
                setSporeReleased(server)
                server.tell('§4[Spore] §cThe infection has been released by an administrator.')
                return 1
            })
    )

    // Re-lock spawning for administration/testing without re-arming the cinematic.
    event.register(
        Commands.literal('spore_lock')
            .requires(source => source.hasPermission(2))
            .executes(ctx => {
                const server = ctx.source.getServer()
                setSporeLocked(server, false)
                server.tell('§2[Spore] §aSpore spawning is locked.')
                return 1
            })
    )

    // Development/reset command: lock Spore and allow the canister event to fire again.
    event.register(
        Commands.literal('spore_reset_outbreak')
            .requires(source => source.hasPermission(2))
            .executes(ctx => {
                const server = ctx.source.getServer()
                setSporeLocked(server, true)
                server.tell('§2[Spore] §aOutbreak state reset and Spore spawning locked.')
                return 1
            })
    )
})
