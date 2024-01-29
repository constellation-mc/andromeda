### What's New:

[`world/crop_temperature`]:

`min`, `max`, `aMin`, `aMax` no longer accept string values. `"1.5"` -> `1.5`

[`general`]:

* Switched to Codecs for JSON parsing.
* `ScopedConfigs#get` no longer looks up a `PersistentState`.
* Bumped min loader version to 0.15.6