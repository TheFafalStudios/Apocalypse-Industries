// Weather2 Toxic Rain v0.3
// Minecraft 1.21.1 NeoForge + KubeJS 7.x
//
// Weather2 Compat redirects Level.isRainingAt(BlockPos) to Weather2's
// localized rain logic. This script never creates or controls weather.

// ============================================================
// CONFIG
// ============================================================

const TOXIC_RAIN = {
  checkIntervalTicks: 20, // 20 ticks = 1 second
  maxExposure: 100,
  exposureGain: 4,
  recoveryRate: 2,

  warningThreshold: 20,
  slowThreshold: 40,
  poisonThreshold: 60,
  severeThreshold: 85,

  effectDurationTicks: 60
}

const EXPOSURE_KEY = 'weather2ToxicRainExposure'
const TIER_KEY = 'weather2ToxicRainTier'
const LAST_TICK_KEY = 'weather2ToxicRainLastProcessedTick'

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value))
}

function exposureTier(exposure) {
  if (exposure >= TOXIC_RAIN.severeThreshold) return 4
  if (exposure >= TOXIC_RAIN.poisonThreshold) return 3
  if (exposure >= TOXIC_RAIN.slowThreshold) return 2
  if (exposure >= TOXIC_RAIN.warningThreshold) return 1
  return 0
}

function notifyTierChange(player, oldTier, newTier) {
  if (oldTier === newTier) return

  // Exposure is getting WORSE.
  if (newTier > oldTier) {
    if (newTier === 1) {
      player.tell('§eYour skin starts to burn in the toxic rain.')
    } else if (newTier === 2) {
      player.tell('§6Toxic exposure is building. Find shelter.')
    } else if (newTier === 3) {
      player.tell('§cYou are being poisoned by the rain!')
    } else if (newTier === 4) {
      player.tell('§4SEVERE TOXIC EXPOSURE — GET UNDER COVER!')
    }
    return
  }

  // Exposure is getting BETTER.
  if (newTier === 3) {
    player.tell('§6Severe toxic exposure is subsiding.')
  } else if (newTier === 2) {
    player.tell('§eThe poisoning is easing. Stay under cover.')
  } else if (newTier === 1) {
    player.tell('§aYour symptoms are fading.')
  } else if (newTier === 0) {
    player.tell('§aToxic exposure has cleared.')
  }
}

function applyExposureEffects(player, exposure) {
  const effects = player.potionEffects

  if (exposure >= TOXIC_RAIN.severeThreshold) {
    effects.add('minecraft:poison', TOXIC_RAIN.effectDurationTicks, 1)
    effects.add('minecraft:weakness', TOXIC_RAIN.effectDurationTicks, 1)
    effects.add('minecraft:slowness', TOXIC_RAIN.effectDurationTicks, 0)
    return
  }

  if (exposure >= TOXIC_RAIN.poisonThreshold) {
    effects.add('minecraft:poison', TOXIC_RAIN.effectDurationTicks, 0)
    effects.add('minecraft:weakness', TOXIC_RAIN.effectDurationTicks, 0)
    return
  }

  if (exposure >= TOXIC_RAIN.slowThreshold) {
    effects.add('minecraft:weakness', TOXIC_RAIN.effectDurationTicks, 0)
    effects.add('minecraft:slowness', TOXIC_RAIN.effectDurationTicks, 0)
    return
  }

  if (exposure >= TOXIC_RAIN.warningThreshold) {
    effects.add('minecraft:weakness', TOXIC_RAIN.effectDurationTicks, 0)
  }
}

function processPlayer(player, serverTick) {
  const data = player.persistentData
  let exposure = data.getInt(EXPOSURE_KEY)

  // Weather2 Compat makes this localized Weather2 rain-aware.
  // isRainingAt also checks actual exposure to the sky.
  const rainPos = player.blockPosition().above()
  const exposedToToxicRain = player.level.isRainingAt(rainPos)

  if (exposedToToxicRain) {
    exposure += TOXIC_RAIN.exposureGain
  } else {
    exposure -= TOXIC_RAIN.recoveryRate
  }

  exposure = clamp(exposure, 0, TOXIC_RAIN.maxExposure)

  const oldTier = data.getInt(TIER_KEY)
  const newTier = exposureTier(exposure)

  data.putInt(EXPOSURE_KEY, exposure)
  data.putInt(TIER_KEY, newTier)
  data.putLong(LAST_TICK_KEY, serverTick)

  notifyTierChange(player, oldTier, newTier)
  applyExposureEffects(player, exposure)
}

// ============================================================
// MAIN MECHANIC
// ============================================================

// v0.2: server-level tick throttling.
// This avoids relying on player.age / player tick bean behavior.
ServerEvents.tick(event => {
  const tick = event.server.tickCount

  if (tick % TOXIC_RAIN.checkIntervalTicks !== 0) return

  event.server.players.forEach(player => {
    processPlayer(player, tick)
  })
})

// ============================================================
// DIAGNOSTIC COMMAND
// ============================================================

ServerEvents.basicCommand('toxicrain_status', event => {
  const player = event.player
  const data = player.persistentData
  const exposure = data.getInt(EXPOSURE_KEY)
  const rainingHere = player.level.isRainingAt(player.blockPosition().above())
  const lastTick = data.getLong(LAST_TICK_KEY)
  const serverTick = event.server.tickCount
  const ticksSinceProcess = lastTick > 0 ? (serverTick - lastTick) : -1

  player.tell(
    `§7Toxic rain: §f${rainingHere ? 'EXPOSED' : 'sheltered/clear'}§7 | ` +
    `Exposure: §f${exposure}/${TOXIC_RAIN.maxExposure}§7 | ` +
    `Last processed: §f${ticksSinceProcess >= 0 ? ticksSinceProcess + ' ticks ago' : 'NEVER'}`
  )
})
