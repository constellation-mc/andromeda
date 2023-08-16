### What's New:

* The big highlight for today is the new FeatureManager.
* The FeatureManager will configure the mod for you in case of certain conflicts. Be it conflicting features or mods.
* By default, the FeatureManager will check for the following conditions:
    * If the game is running with [Connector](https://modrinth.com/mod/connector). (Enables compatibility mode and disables some other features).
    * If either "Safe Beds" or "Beds Explode Everywhere" is enabled.
    * When Iceberg <1.1.13 is loaded. (Disables "Tooltip, not Name" and "Item Frame Tooltips").
* Mod developers can add custom FeatureProcessors using either the `andromeda:feature_manager` entrypoint (you must implement the `Runnable` interface).
* Or by using the `custom` block in their `fabric.mod.json`.
* Either way, you can read more on the wiki.

***

Other minor things.

* Removed the incubator tooltip and rarity.
* There's now a 24-hour interval in between translation updates.
* Made log messages a little cleaner.
* Fixed `NoSuchFileException` during loading.
* Fixed `No data fixer registered for andromeda_*` warnings.
* Fixed running on [Connector](https://modrinth.com/mod/connector)
* Updated Chinese translation. Courtesy of [Rad233](https://github.com/Rad233).
* Updated Dark Matter