package me.melontini.andromeda.base.config;

import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import org.jetbrains.annotations.ApiStatus;

@Getter
@Setter
public class BasicConfig {
    @ApiStatus.Internal
    @ConfigEntry.Gui.RequiresRestart
    public boolean enabled = false;
}
