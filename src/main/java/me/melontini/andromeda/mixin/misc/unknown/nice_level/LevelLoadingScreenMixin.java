package me.melontini.andromeda.mixin.misc.unknown.nice_level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.MixinEnvironment;
import me.melontini.andromeda.modules.misc.unknown.Unknown;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@MixinEnvironment(EnvType.CLIENT)
@Mixin(LevelLoadingScreen.class)
class LevelLoadingScreenMixin {
    @Unique
    private static final Unknown am$unk = ModuleManager.quick(Unknown.class);

    @ModifyReturnValue(at = @At("RETURN"), method = "getPercentage")
    private String andromeda$getPercentage(String o) {
        if (am$unk.config().enabled) {
            if (Objects.equals(o, "69%")) {
                return "Nice%";
            }
        }
        return o;
    }
}
