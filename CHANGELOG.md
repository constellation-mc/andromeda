### What's New:

[`world/crop_temperature`]:

* Bamboo, grass blocks and a few other modded plants are now supported.
* Fixed broken expression support.
* Updated format to be a little more sensible.

```json5
{
  "replace": false,
  "entries": {
    "minecraft:beetroots": [
      0.2, // abs min
      0.3, // min
      1.0, // max
      1.0  // abs max
    ],
  }
}
```

[`blocks/incubator`]:

* The format now supports `replace`.

[`general`]:

* Moved misc client data to a single JSON file.
* Tried to fix a rare CME in `MixinProcessor`. (I have no idea if this works)
* Updated Dark Matter.