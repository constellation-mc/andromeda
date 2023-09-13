package me.melontini.andromeda.mixin.misc.unknown.nice_level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelLoadingScreen.class)
@MixinRelatedConfigOption("unknown")
public class LevelLoadingScreenMixin {

    @Shadow @Final private WorldGenerationProgressTracker progressProvider;

    @ModifyReturnValue(at = @At("RETURN"), method = "getPercentage")
    private Text andromeda$getPercentage(Text o) {
        if (Config.get().unknown) {
            if (this.progressProvider.getProgressPercentage() == 69) {
                return TextUtil.translatable("loading.progress", "Nice%");
            }
        }
        return o;
    }
}
