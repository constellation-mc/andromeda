package dev.zenfyr.andromeda.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import net.fabricmc.api.EnvType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@MixinEnvironment(EnvType.CLIENT)
@Mixin(SubCategoryListEntry.class)
abstract class SubCategoryListEntryMixin {

  // Why am I fixing CC bugs?
  @ModifyExpressionValue(
      method = "isRequiresRestart",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lme/shedaniel/clothconfig2/api/AbstractConfigListEntry;isRequiresRestart()Z"))
  private boolean andromeda$isEdited(boolean original, @Local AbstractConfigListEntry<?> e) {
    return original && e.isEdited();
  }
}
