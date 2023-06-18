package me.melontini.andromeda.mixin.gui.gui_particles.accessors;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x")
    int andromeda$getX();

    @Accessor("y")
    int andromeda$getY();
}
