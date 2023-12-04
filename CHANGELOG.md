### What's New:

## Alpha warning!

- In v1.0.0 Andromeda has been rewritten in a fairly major way. There were a lot of breaking changes!
- This release does not function properly on Connector and requires some complicated fixes on its side. 
- I suggest reading the "State of Andromeda" post I made on GitHub for more context and changes in this version!

### https://github.com/melontini/andromeda/discussions/63

Something not mentioned in the post:

- All resources were moved from `am_{something}` to `andromeda/{something}`
- There's no config migration, so you need to re-configure the mod again, sorry.
- `throwable_items:blacklist` is now part of the data pack as `"disabled": true`.
- Added `andromeda:tempting_for_villagers` tag to specify items villagers will follow.
- Throwable Items can now be dispensed!