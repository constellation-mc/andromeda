package me.melontini.andromeda.mixin.misc.unknown.nice_level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.andromeda.util.annotations.MixinEnvironment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@MixinEnvironment(EnvType.CLIENT)
@Mixin(LevelLoadingScreen.class)
@Feature("unknown")
class LevelLoadingScreenMixin {

    @ModifyReturnValue(at = @At("RETURN"), method = "getPercentage")
    private String andromeda$getPercentage(String o) {
        if (Config.get().unknown) {
            if (Objects.equals(o, "69%")) {
                return "Nice%";
            }
        }
        return o;
    }
}
