package me.melontini.andromeda.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.dark_matter.api.base.util.mixin.annotations.MixinPredicate;
import me.melontini.dark_matter.api.base.util.mixin.annotations.Mod;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@MixinPredicate(mods = @Mod("cloth-config"))
@SpecialEnvironment(Environment.CLIENT)
@Mixin(SubCategoryListEntry.class)
abstract class SubCategoryListEntryMixin {

    //Why am I fixing CC bugs?
    @ModifyExpressionValue(method = "isRequiresRestart", at = @At(value = "INVOKE", target = "Lme/shedaniel/clothconfig2/api/AbstractConfigListEntry;isRequiresRestart()Z"))
    private boolean andromeda$isEdited(boolean original, @Local AbstractConfigListEntry<?> e) {
        return original && e.isEdited();
    }
}
