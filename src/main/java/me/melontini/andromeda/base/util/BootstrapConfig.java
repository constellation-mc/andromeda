package me.melontini.andromeda.base.util;

import lombok.Data;
import lombok.experimental.Accessors;

@Data @Accessors(fluent = true)
public final class BootstrapConfig {
    public boolean enabled = false;
    public Scope scope = Scope.GLOBAL;

    public enum Scope {
        GLOBAL, WORLD, DIMENSION;

        public boolean isWorld() {
            return this == WORLD;
        }

        public boolean isGlobal() {
            return this == GLOBAL;
        }

        public boolean isDimension() {
            return this == DIMENSION;
        }
    }
}
