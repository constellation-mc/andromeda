package me.melontini.andromeda.base;

import me.melontini.andromeda.util.annotations.config.Environment;

public interface Module {

    default void onClient() { }
    default void onServer() { }
    default void onMain() { }
    default void onPreLaunch() { }

    default Environment environment() {
        return Environment.BOTH;
    }

    boolean enabled();
}
