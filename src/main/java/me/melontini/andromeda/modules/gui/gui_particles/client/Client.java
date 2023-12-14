package me.melontini.andromeda.modules.gui.gui_particles.client;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.gui.gui_particles.GuiParticles;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.particle.ParticleTypes;

public class Client {

    public static void init() {
        GuiParticles module = ModuleManager.quick(GuiParticles.class);

        ScreenEvents.BEFORE_INIT.register((client, screen1, scaledWidth, scaledHeight) -> {
            if (screen1 instanceof AbstractFurnaceScreen<?> abstractFurnaceScreen && module.config().furnaceScreenParticles) {
                ScreenEvents.afterTick(abstractFurnaceScreen).register(screen -> {
                    AbstractFurnaceScreen<?> furnaceScreen = (AbstractFurnaceScreen<?>) screen;
                    if (furnaceScreen.getScreenHandler().isBurning() && MathStuff.threadRandom().nextInt(10) == 0) {
                        ScreenParticleHelper.addScreenParticle(screen, ParticleTypes.FLAME,
                                MathStuff.nextDouble(furnaceScreen.x + 56, furnaceScreen.x + 56 + 14),
                                furnaceScreen.y + 36 + 13, MathStuff.nextDouble(-0.01, 0.01),
                                0.05);
                    }
                });
            }
        });
    }
}
