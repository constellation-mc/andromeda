# What's New:

- New Module! `[items/fire_damage]`!
- More internal cleanups

## `[blocks/guarded_loot]`

- Moved the lockpick option to the `items/lockpick` module.

## `[entities/boats]`

- Rewrote logic to calculate collisions for the TNT Boat on the server, no longer blindly trusting the client.

## `[items/fire_damage]`

- New Module! Apply damage to wooden and leather items when set on fire! Also disables the shield.

## `[items/infinite_totem]`

- Rewrote the logic to not spam velocity updates every tick, requires update on the client!

## `[items/magnet]`

- GUI Particles are now controlled by the `gui/gui_particles` module.
- The "progress" bar for the magnet is now hidden, it's not accurate as, unlike the bundle, tha magnet can track a basically unlimited amount items.

## `[items/pouches]`

- Added `command` loot context to loot tables.
- Reduced the amount of dropped items.

## `[items/tooltips]`

- Broken tooltips, like clock outside the overworld or unlinked compasses, will now show obfuscated text instead of glitching weirdly. 
Should make using with `gui/name_tooltips` way nicer.

## `[misc/recipe_advancements_generation]`

- The module now skips recipes if they already have an advancement for them.

## `[misc/translations]`

- Moved downloads to a background executor.
- Downloads on lang change shouldn't block the main thread.

## `[world/falling_beenests]`

- Added `block` loot context to the loot table.
- Reduced the amount of dropped honeycombs.