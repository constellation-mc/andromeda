package me.melontini.andromeda.base.util.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Data @Accessors(fluent = true)
public final class BootstrapConfig {
    public boolean enabled = false;

    public static @NotNull BootstrapConfig create(boolean on) {
        var config = new BootstrapConfig();
        if (on) config.enabled = true;
        return config;
    }
}
