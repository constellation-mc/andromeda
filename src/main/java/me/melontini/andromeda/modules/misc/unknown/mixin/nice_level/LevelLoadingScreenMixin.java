package me.melontini.andromeda.modules.misc.unknown.mixin.nice_level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.server.WorldGenerationProgressTracker;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@SpecialEnvironment(Environment.CLIENT)
@Mixin(LevelLoadingScreen.class)
abstract class LevelLoadingScreenMixin {

    @Shadow @Final private WorldGenerationProgressTracker progressProvider;

    @ModifyReturnValue(at = @At("RETURN"), method = "getPercentage")
    private Text andromeda$getPercentage(Text original) {
        if (this.progressProvider.getProgressPercentage() == 69) {
            return Text.literal("Nice%");
        }
        return original;
    }
}
