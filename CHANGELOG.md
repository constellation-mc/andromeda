### What's New:

[`gui/smooth_tooltips`]:

* New Module!
* Makes tooltips slowly flow towards the cursor. 
* Compatibility can be hit & miss, so experimental, for now.

[`mechanics/dragon_fight`]:

* Fixed the `ConcurrentModificationException` crash.
* Moved data to a new format. **All data has been reset.**

[`mechanics/trading_goat_horn`]:

* Moved data to a new format. **All data has been reset.**

[`general`]:

* Updated Chinese translation. Courtesy of [Rad586](https://github.com/Rad586).
* Startup should be a bit faster now.
  * Andromeda used to scan every module's package for classes `Main`, `Merged`, `client.Client` and `server.Server`.
  * Now, module entrypoints are defined and registered during module construction.
* Other minor changes.