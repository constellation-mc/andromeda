package me.melontini.andromeda.common.mixin;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRules.Key.class)
class GameRuleKeyMixin {

    @Shadow
    @Final
    private String name;

    @Inject(at = @At("HEAD"), method = "getTranslationKey", cancellable = true)
    private void andromeda$modifyTranslationKey(CallbackInfoReturnable<String> cir) {
        if (this.name.startsWith("andromeda:")) {
            String s = this.name.substring("andromeda:".length());

            int index = s.lastIndexOf(':');
            cir.setReturnValue("config.andromeda." + s.substring(0, index) + ".option." + s.substring(index + 1));
        }
    }
}
