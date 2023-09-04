package me.melontini.andromeda.registries;

import net.minecraft.util.Identifier;

import static me.melontini.andromeda.util.SharedConstants.MODID;

class Common {

    static Identifier id(String path) {
        return new Identifier(MODID, path);
    }
}
