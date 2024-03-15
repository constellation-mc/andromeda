package me.melontini.andromeda.modules.gui.smooth_tooltips.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.gui.smooth_tooltips.SmoothTooltips;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DrawContext.class)
abstract class DrawContextMixin {

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    @Final
    private MatrixStack matrices;

    @Unique
    private static final SmoothTooltips m = ModuleManager.quick(SmoothTooltips.class);

    @Unique
    private static Vector2d smoothPos;

    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/tooltip/TooltipPositioner;getPosition(IIIIII)Lorg/joml/Vector2ic;"), method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V")
    private Vector2ic andromeda$smoothTooltip(Vector2ic vic, @Local(argsOnly = true, ordinal = 0) int x, @Local(argsOnly = true, ordinal = 1) int y, @Share("popMatrix") LocalBooleanRef popMatrix) {
        if (andromeda$makeSmooth(x, y)) {
            if (smoothPos == null) smoothPos = new Vector2d(x, y);
            smoothPos.x = MathHelper.clamp(MathHelper.lerp(m.config().deltaX * client.getLastFrameDuration(), smoothPos.x, vic.x()), vic.x() - m.config().clampX, vic.x() + m.config().clampX);
            smoothPos.y = MathHelper.clamp(MathHelper.lerp(m.config().deltaY * client.getLastFrameDuration(), smoothPos.y, vic.y()), vic.y() - m.config().clampY, vic.y() + m.config().clampY);

            popMatrix.set(true);
            this.matrices.push();
            this.matrices.translate(smoothPos.x - (int) smoothPos.x, smoothPos.y - (int) smoothPos.y, 1);
            return new Vector2i((int) smoothPos.x, (int) smoothPos.y);
        }
        return vic;
    }

    @Unique
    private boolean andromeda$makeSmooth(int x, int y) {
        double mX = (this.client.mouse.getX() * this.client.getWindow().getScaledWidth() / this.client.getWindow().getWidth());
        if ((int) mX != x) return false;
        double mY = (this.client.mouse.getY() * this.client.getWindow().getScaledHeight() / this.client.getWindow().getHeight());
        return (int) mY == y;
    }

    @Inject(at = @At(value = "TAIL"), method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V")
    private void andromeda$popMatrix(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, CallbackInfo ci, @Share("popMatrix") LocalBooleanRef popMatrix) {
        if (popMatrix.get()) this.matrices.pop();
    }
}
