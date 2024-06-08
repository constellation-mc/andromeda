package me.melontini.andromeda.base.util.config;

import lombok.Data;
import lombok.experimental.Accessors;

@Data @Accessors(fluent = true)
public final class BootstrapConfig {
    public boolean enabled = false;
}
