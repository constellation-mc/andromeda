# What's New:

- Fixed registry entries in configs and data packs not raising an error for missing blocks/items and instead falling back to the default value.
- Removed a bunch of legacy stuff.
- Updated Pulsar to 0.3.0

## `[blocks/campfire_effects]`:

- Moved the entire config to data packs. See the wiki for more details:

`/data/[namespace]/andromeda/campfire_effects/campfire.json`

```json5
{
  // block identifier, often different from the item id!
  "minecraft:campfire": {
    "range": 10.0, // Range in blocks.
    "affectsPassive": true, // If passive entities should be affected, only players otherwise.
    "condition": {
      // Optional loot condition, global. supports 'origin', 'block_state', 'block_entity'
      "condition": "minecraft:block_state_property",
      "block": "minecraft:campfire",
      "properties": {
        "lit": "true"
      }
    },
    "effects": [
      {
        "effect": "minecraft:regeneration", // Effect identifier
        "amplifier": 0 // Effect amplifier.
        // "condition": Optional loot condition, per entity. supports the globals + 'this_entity'
      }
    ]
  }
}
```

## `[items/magnet]`:

- Fixed an issue where all magnets in an inventory would activate when having one selected.

## `[mechanics/trading_goat_horn]`:

- The `instrumentId` config entry was moved to an instrument tag `andromeda:trader_songs`.

## `[misc/minor_inconvenience]`:

- Added a load condition to only load the damage type when the module is on.

## `[world/auto_planting]`:

- The `idList` config entry was moved to an item tag `andromeda:auto_planting`, the list mode is still controlled by the config.

## `[world/crop_temperature]`:

- Updated the data pack format again, the temperature list in now wrapped in an object:

```json5
{
  // block identifier, often different from the item id!
  "minecraft:beetroots": {
    "temperatures": [
      0.2, // absolute minimum; won't grow below this; inclusive
      0.3, // minimum, will grow; but slowly; exclusive
      1.0, // maximum, will grow; but slowly; exclusive
      1.0, // absolute maximum; won't grow above this; inclusive
    ]
  }
}
```

- Removed the old new and the old old data pack formats.