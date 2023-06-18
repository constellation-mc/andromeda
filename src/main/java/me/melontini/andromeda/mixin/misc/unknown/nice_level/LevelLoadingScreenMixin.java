package me.melontini.andromeda.mixin.misc.unknown.nice_level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(LevelLoadingScreen.class)
@MixinRelatedConfigOption("unknown")
public class LevelLoadingScreenMixin {

    @ModifyReturnValue(at = @At("RETURN"), method = "getPercentage")
    private String andromeda$getPercentage(String o) {
        if (Andromeda.CONFIG.unknown) {
            if (Objects.equals(o, "69%")) {
                return "Nice%";
            }
        }
        return o;
    }
}
