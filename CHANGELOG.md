### What's New:

#### Oops! All zombies!

New Tweaks:
* Can Zombies Throw Items?
  * An extension of "Throwable Items".
  * Allows zombies to throw items with behaviors.
* Zombies Don't Pickup Garbage
  * Prevents zombies from picking up garbage with no use.
* All Zombies Can Pick up Items
  * Gives all zombies the ability to pickup items.

***

* Added `fabric:load_conditions` support to mod's resources.
* Tried to improve JSON parsing. It should be faster* and have better error messages.
* Moved `FeatureConfig` and `ItemBehaviors` to `api`. You still need to use their entrypoints.
* Added `andromeda:pre/post-main` and `andromeda:pre/post-client` entrypoints.
* Removed BoatEntityMixin in favor of AWs and an `@Override` in FurnaceBoatEntity.
* Updated Chinese translation. Courtesy of [Rad233](https://github.com/Rad233).
* Updated Dark Matter.