// Apocalypse Industries - Farming Core Test v1
// Minecraft 1.21.1 / NeoForge / KubeJS 7.x
//
// Goals:
// 1) Food crops require REAL daylight rather than torch light.
// 2) Farmland with a moisture property must actually be hydrated.
// 3) Weather2 localized toxic rain stops exposed crop growth and can regress crops.
// 4) A normal glass roof remains a valid greenhouse: skylight passes, rain exposure does not.
//
// Weather is NOT controlled here. weather2compat already redirects Level.isRainingAt(BlockPos)
// to Weather2's localized rain checks in this pack.

const $FarmCropGrowPre = Java.loadClass('net.neoforged.neoforge.event.level.block.CropGrowEvent$Pre')
const $FarmCropGrowResult = Java.loadClass('net.neoforged.neoforge.event.level.block.CropGrowEvent$Pre$Result')
const $FarmLightLayer = Java.loadClass('net.minecraft.world.level.LightLayer')
const $FarmCropBlock = Java.loadClass('net.minecraft.world.level.block.CropBlock')
const $FarmStemBlock = Java.loadClass('net.minecraft.world.level.block.StemBlock')
const $FarmBlockStateProperties = Java.loadClass('net.minecraft.world.level.block.state.properties.BlockStateProperties')
const $FarmBuiltInRegistries = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries')

const APOCALYPSE_FARMING_TEST = {
  // Regular glass keeps sky light high enough; opaque roofs and torch-lit bunkers do not.
  minimumSkyLight: 12,

  // Crops only progress during the daylight portion of the day.
  requireDaytime: true,

  // Vanilla farmland normally permits very slow growth when dry. This test makes water mandatory.
  requireHydratedFarmland: true,

  // Any localized Weather2 rain is already treated as toxic rain elsewhere in this pack.
  // Exposed crops cannot progress while it is raining at their position.
  blockGrowthInToxicRain: true,

  // On each attempted growth tick while exposed to toxic rain, ordinary CropBlock crops have
  // a small chance to lose one age stage. This is intentionally noticeable but not catastrophic.
  toxicRainRegressionChance: 0.06,

  // Farmer's Delight crops which are not guaranteed to inherit vanilla CropBlock/StemBlock.
  explicitCropIds: [
    'farmersdelight:cabbages',
    'farmersdelight:onions',
    'farmersdelight:budding_tomatoes',
    'farmersdelight:tomatoes',
    'farmersdelight:tomatoes_on_rope'
  ]
}

function apocalypseFarmBlockId(block) {
  return String($FarmBuiltInRegistries.BLOCK.getKey(block))
}

function apocalypseFarmIsManagedCrop(state) {
  const block = state.getBlock()

  // This automatically covers vanilla wheat/carrots/potatoes/beetroot and many modded crops.
  if (block instanceof $FarmCropBlock || block instanceof $FarmStemBlock) return true

  const id = apocalypseFarmBlockId(block)
  return APOCALYPSE_FARMING_TEST.explicitCropIds.indexOf(id) !== -1
}

function apocalypseFarmSkyLight(level, pos) {
  return level.getBrightness($FarmLightLayer.SKY, pos)
}

function apocalypseFarmIsHydrated(level, cropPos) {
  const soilState = level.getBlockState(cropPos.below())

  // Only enforce hydration for farmland-like blocks which actually expose Minecraft's moisture property.
  // This avoids breaking unusual modded hydroponic/planter substrates by accident.
  if (!soilState.hasProperty($FarmBlockStateProperties.MOISTURE)) return true

  return Number(soilState.getValue($FarmBlockStateProperties.MOISTURE)) > 0
}

function apocalypseFarmRegressStandardCrop(level, pos, state) {
  const block = state.getBlock()

  // CropBlock has safe public age helpers. Do not guess state properties on unusual modded crops.
  if (!(block instanceof $FarmCropBlock)) return false

  const age = block.getAge(state)
  if (age <= 0) return false

  level.setBlock(pos, block.getStateForAge(age - 1), 3)
  return true
}

NativeEvents.onEvent($FarmCropGrowPre, event => {
  const level = event.getLevel()
  const pos = event.getPos()
  const state = event.getState()

  if (!apocalypseFarmIsManagedCrop(state)) return

  // Artificial block light is intentionally ignored. Only sky light counts.
  if (APOCALYPSE_FARMING_TEST.requireDaytime && !level.isDay()) {
    event.setResult($FarmCropGrowResult.DO_NOT_GROW)
    return
  }

  if (apocalypseFarmSkyLight(level, pos) < APOCALYPSE_FARMING_TEST.minimumSkyLight) {
    event.setResult($FarmCropGrowResult.DO_NOT_GROW)
    return
  }

  if (APOCALYPSE_FARMING_TEST.requireHydratedFarmland && !apocalypseFarmIsHydrated(level, pos)) {
    event.setResult($FarmCropGrowResult.DO_NOT_GROW)
    return
  }

  // Weather2 Compat makes this a localized Weather2 rain query and also respects shelter.
  if (APOCALYPSE_FARMING_TEST.blockGrowthInToxicRain && level.isRainingAt(pos.above())) {
    if (level.random.nextFloat() < APOCALYPSE_FARMING_TEST.toxicRainRegressionChance) {
      apocalypseFarmRegressStandardCrop(level, pos, state)
    }

    event.setResult($FarmCropGrowResult.DO_NOT_GROW)
  }
})

// Lightweight diagnostic command for testing. Stand in/on a crop and run /farmtest_status.
ServerEvents.basicCommand('farmtest_status', event => {
  const player = event.player
  const level = player.level
  const basePos = player.blockPosition()

  let pos = basePos
  let state = level.getBlockState(pos)

  if (!apocalypseFarmIsManagedCrop(state)) {
    const below = basePos.below()
    const belowState = level.getBlockState(below)
    if (apocalypseFarmIsManagedCrop(belowState)) {
      pos = below
      state = belowState
    }
  }

  if (!apocalypseFarmIsManagedCrop(state)) {
    player.tell('§eFarm test: stand in/on a managed crop, then run /farmtest_status again.')
    return
  }

  const id = apocalypseFarmBlockId(state.getBlock())
  const sky = apocalypseFarmSkyLight(level, pos)
  const day = level.isDay()
  const hydrated = apocalypseFarmIsHydrated(level, pos)
  const toxicRain = level.isRainingAt(pos.above())
  const wouldGrow = (!APOCALYPSE_FARMING_TEST.requireDaytime || day) &&
    sky >= APOCALYPSE_FARMING_TEST.minimumSkyLight &&
    (!APOCALYPSE_FARMING_TEST.requireHydratedFarmland || hydrated) &&
    (!APOCALYPSE_FARMING_TEST.blockGrowthInToxicRain || !toxicRain)

  player.tell(
    `§7Farm test §8| §f${id} §8| ` +
    `§7day=§f${day} §7sky=§f${sky} §7hydrated=§f${hydrated} ` +
    `§7toxicRain=§f${toxicRain} §8| ` +
    `§7growth=§f${wouldGrow ? 'ALLOWED' : 'BLOCKED'}`
  )
})

console.info('[Apocalypse Industries] Farming Core Test v1 loaded: daylight + hydration + Weather2 exposure')
