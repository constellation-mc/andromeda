package me.melontini.andromeda.mixin.misc.auto_config;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.shedaniel.autoconfig.gui.registry.ComposedGuiRegistryAccess;
import me.shedaniel.autoconfig.gui.registry.DefaultGuiRegistryAccess;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = ComposedGuiRegistryAccess.class)
public class ComposedGuiRegistryAccessMixin {

    @ModifyExpressionValue(at = @At(value = "FIELD", target = "Lme/shedaniel/autoconfig/gui/registry/ComposedGuiRegistryAccess;children:Ljava/util/List;"), method = "transform", remap = false)
    public List<GuiRegistryAccess> transform(List<GuiRegistryAccess> children) {
        //We need to reverse children order to allow custom transformations to be applied.
        if (children.get(children.size() - 1) instanceof DefaultGuiRegistryAccess) {
            return Lists.reverse(children);
        }

        return children;
    }
}
