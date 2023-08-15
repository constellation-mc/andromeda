package me.melontini.andromeda.mixin.misc.auto_config;

import com.google.common.collect.Lists;
import me.shedaniel.autoconfig.gui.registry.ComposedGuiRegistryAccess;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;
import java.util.List;

@Mixin(ComposedGuiRegistryAccess.class)
public class ComposedGuiRegistryAccessMixin {
    @Shadow(remap = false) private List<GuiRegistryAccess> children;

    /**
     * @author melontini
     * @reason incorrect children order
     */
    @Overwrite(remap = false)
    public List<AbstractConfigListEntry> transform(List<AbstractConfigListEntry> guis, String i18n, Field field, Object config, Object defaults, GuiRegistryAccess registry) {
        //We need to reverse children order to allow custom transformations to be applied.
        for(GuiRegistryAccess child : Lists.reverse(this.children)) {
            guis = child.transform(guis, i18n, field, config, defaults, registry);
        }

        return guis;
    }
}
