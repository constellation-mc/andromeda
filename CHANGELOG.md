### What's New:

So, I may have lied a bit and may or may not have refactored half of the mod's internals.

[`world/crop_temperature`]:

`min`, `max`, `aMin`, `aMax` no longer accept string values. `"1.5"` -> `1.5`

[`entities/boats`]:

* Added missing dispenser behavior.

[`misc/recipe_advancements_generation`]:

* Reworked advancement generation. More recipe types should be supported by default!

[`general`]:

* Switched to Codecs for JSON parsing.
* `ScopedConfigs#get` no longer looks up a `PersistentState`.
* Bumped the minimum loader version to 0.15.6.
* Moved optional module methods to mini events.
* Module construction has been delayed to account for events.
* Added error handling to `BootstrapExtension`.
* Simplified the horrible `Common.bootstrap` and `Keeper` system. Keepers are now initialized by the module's `Main`/`Client`/etc. class constructors.
* Probably fixed a few other things.