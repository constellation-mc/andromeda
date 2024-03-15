package me.melontini.andromeda.modules.gui.smooth_tooltips.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.gui.smooth_tooltips.SmoothTooltips;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
abstract class ScreenMixin {

    @Unique
    private static final SmoothTooltips m = ModuleManager.quick(SmoothTooltips.class);

    @Shadow
    private MinecraftClient client;

    @Unique
    private static double smoothX;
    @Unique
    private static double smoothY;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.BEFORE), method = "renderTooltipFromComponents")
    private void andromeda$smoothTooltip(CallbackInfo ci, @Local(argsOnly = true) MatrixStack stack, @Local(ordinal = 0, argsOnly = true) int x, @Local(ordinal = 1, argsOnly = true) int y, @Local(index = 7) LocalIntRef targetX, @Local(index = 8) LocalIntRef targetY, @Share("popMatrix") LocalBooleanRef popMatrix) {
        if (andromeda$makeSmooth(x, y)) {
            smoothX = MathHelper.clamp(MathHelper.lerp(m.config().deltaX * client.getLastFrameDuration(), smoothX, targetX.get()), targetX.get() - m.config().clampX, targetX.get() + m.config().clampX);
            smoothY = MathHelper.clamp(MathHelper.lerp(m.config().deltaY * client.getLastFrameDuration(), smoothY, targetY.get()), targetY.get() - m.config().clampY, targetY.get() + m.config().clampY);

            popMatrix.set(true);
            stack.push();
            stack.translate(smoothX - (int) smoothX, smoothY - (int) smoothY, 1);
            targetX.set((int) smoothX);
            targetY.set((int) smoothY);
        }
    }

    @Unique
    private boolean andromeda$makeSmooth(int x, int y) {
        double mX = (this.client.mouse.getX() * this.client.getWindow().getScaledWidth() / this.client.getWindow().getWidth());
        if ((int) mX != x) return false;
        double mY = (this.client.mouse.getY() * this.client.getWindow().getScaledHeight() / this.client.getWindow().getHeight());
        return (int) mY == y;
    }

    @Inject(at = @At(value = "TAIL"), method = "renderTooltipFromComponents")
    private void andromeda$popMatrix(CallbackInfo ci, @Local MatrixStack stack, @Share("popMatrix") LocalBooleanRef popMatrix) {
        if (popMatrix.get()) stack.pop();
    }
}
