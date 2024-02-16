package me.melontini.andromeda.base.util;

import net.fabricmc.api.EnvType;

public enum Environment {
    CLIENT,
    SERVER,
    BOTH,
    ANY;

    public boolean allows(EnvType envType) {
        return allows(envType == EnvType.CLIENT ? CLIENT : SERVER);
    }

    public boolean allows(Environment environment) {
        if (this == ANY || this == BOTH) return true;
        return this == environment;
    }

    public boolean isClient() {
        return this == CLIENT;
    }

    public boolean isServer() {
        return this == SERVER;
    }

    public boolean isAny() {
        return this == ANY;
    }

    public boolean isBoth() {
        return this == BOTH;
    }
}
