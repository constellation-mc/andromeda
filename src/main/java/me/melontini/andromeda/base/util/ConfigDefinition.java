package me.melontini.andromeda.base.util;

import me.melontini.andromeda.base.Module;

import java.util.function.Supplier;

public record ConfigDefinition<T extends Module.BaseConfig>(Supplier<Class<T>> supplier) {

}
