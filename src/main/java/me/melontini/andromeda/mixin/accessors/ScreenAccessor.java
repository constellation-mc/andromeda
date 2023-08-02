package me.melontini.andromeda.mixin.accessors;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Accessor("children")
    List<Element> andromeda$getChildren();

    @Accessor("selectables")
    List<Selectable> andromeda$getSelectables();

    @Accessor("drawables")
    List<Drawable> andromeda$getDrawables();
}
