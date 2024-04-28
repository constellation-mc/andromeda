package me.melontini.andromeda.base;

import me.melontini.andromeda.base.util.BootstrapConfig;
import me.melontini.andromeda.base.util.ConfigHandler;

import java.util.function.Function;

record CG(ConfigHandler handler) implements Function<Module<?>, BootstrapConfig> {

    @Override
    public BootstrapConfig apply(Module<?> module) {
        return handler().get(module).e;
    }
}
