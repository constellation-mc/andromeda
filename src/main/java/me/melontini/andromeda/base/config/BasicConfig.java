package me.melontini.andromeda.base.config;

import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Getter
@Setter
public class BasicConfig {
    @ConfigEntry.Gui.RequiresRestart
    public boolean enabled = false;
}
