package me.melontini.andromeda.mixin.bugfixes.advancement_shadow;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.bugfixes.advancement_shadow.AdvancementShadow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AdvancementTab.class)
class AdvancementTabMixin {
    @Unique
    private static final AdvancementShadow am$fias = ModuleManager.quick(AdvancementShadow.class);

    @Shadow private float alpha;
    @Shadow @Final private MinecraftClient client;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal = 0), index = 0, method = "drawWidgetTooltip")
    private float andromeda$draw(float value) {
        return am$fias.config().enabled ? this.alpha + (0.04F * client.getLastFrameDuration()) : value;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal = 1), index = 0, method = "drawWidgetTooltip")
    private float andromeda$draw1(float value) {
        return am$fias.config().enabled ? this.alpha - (0.06F * client.getLastFrameDuration()) : value;
    }
}
