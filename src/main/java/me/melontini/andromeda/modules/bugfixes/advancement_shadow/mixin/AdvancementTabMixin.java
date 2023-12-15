package me.melontini.andromeda.modules.bugfixes.advancement_shadow.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AdvancementTab.class)
class AdvancementTabMixin {

    @Shadow private float alpha;
    @Shadow @Final private MinecraftClient client;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal = 0), index = 0, method = "drawWidgetTooltip")
    private float andromeda$draw(float value) {
        return this.alpha + (0.04F * client.getLastFrameDuration());
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal = 1), index = 0, method = "drawWidgetTooltip")
    private float andromeda$draw1(float value) {
        return this.alpha - (0.06F * client.getLastFrameDuration());
    }
}
