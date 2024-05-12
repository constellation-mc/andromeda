### What's New:

#### BACK UP YOUR CONFIGS BEFORE UPDATING!!!

This update overhauls the config system to make it way more flexible and powerful.

[`items/infinite_totem`]:

* Fixed a memory leak.
* The beacon is now required to follow this pattern:

```
    B
   NNN
  DDDDD
 NNNNNNN
DDDDDDDDD
```

Where N is netherite and D is diamond.

[`general`]:

* The configuration system has been reworked.

This update splits module configs into several parts: `bootstrap`, `main`, `game` and `client`.

One of the major additions is the optional [support for Commander expressions in the config](https://andromeda-wiki.pages.dev/misc#config-expressions-👩‍💻). Commander 0.5.0 has some amazing additions and I highly recommend checking them out!

One change that may break datapack configs is the removal of the `enabled` field from scoped configs. The field is now `available` and supports expressions. And on the note of scoped configs, the only config state that supports scopes is `game`, so configs without it will not support scopes.

* Rewrote config screen generation to support expressions.

> constellation.inc is grateful for your continued trust!