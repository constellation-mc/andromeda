package me.melontini.andromeda.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SpecialEnvironment(Environment.CLIENT)
@Mixin(SubCategoryListEntry.class)
class SubCategoryListEntryMixin {

    //Why am I fixing CC bugs?
    @WrapOperation(method = "isRequiresRestart", at = @At(value = "INVOKE", target = "Lme/shedaniel/clothconfig2/api/AbstractConfigListEntry;isRequiresRestart()Z"))
    private boolean andromeda$isEdited(AbstractConfigListEntry<?> instance, Operation<Boolean> original) {
        return original.call(instance) && instance.isEdited();
    }
}
