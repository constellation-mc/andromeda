package me.melontini.andromeda.modules.gui.gui_particles;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.util.JsonOps;

@ModuleTooltip
@ModuleInfo(name = "gui_particles", category = "gui", environment = Environment.CLIENT)
public class GuiParticles extends Module<GuiParticles.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        if (config.has("guiParticles")) {
            JsonObject guiParticles = config.getAsJsonObject("guiParticles");

            this.config().enabled = true;
            JsonOps.ifPresent(guiParticles, "anvilScreenParticles", e -> this.config().anvilScreenParticles = e.getAsBoolean());
            JsonOps.ifPresent(guiParticles, "enchantmentScreenParticles", e -> this.config().enchantmentScreenParticles = e.getAsBoolean());
            JsonOps.ifPresent(guiParticles, "furnaceScreenParticles", e -> this.config().furnaceScreenParticles = e.getAsBoolean());
            JsonOps.ifPresent(guiParticles, "creativeScreenParticles", e -> this.config().creativeScreenParticles = e.getAsBoolean());
            JsonOps.ifPresent(guiParticles, "creativeScreenParticlesVelX", e -> this.config().creativeScreenParticlesVelX = e.getAsDouble());
            JsonOps.ifPresent(guiParticles, "gameModeSwitcherParticles", e -> this.config().gameModeSwitcherParticles = e.getAsBoolean());
        }
    }

    public static class Config extends BasicConfig {

        public boolean anvilScreenParticles = true;

        public boolean enchantmentScreenParticles = true;

        public boolean furnaceScreenParticles = true;

        public boolean creativeScreenParticles = true;

        public double creativeScreenParticlesVelX = 0.7d;

        public boolean gameModeSwitcherParticles = true;

        public boolean bundleInputParticles = true;
    }
}
