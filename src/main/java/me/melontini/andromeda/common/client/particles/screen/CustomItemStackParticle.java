package me.melontini.andromeda.common.client.particles.screen;

import me.melontini.dark_matter.api.glitter.particles.ItemStackParticle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class CustomItemStackParticle extends ItemStackParticle {
    public CustomItemStackParticle(double x, double y, double velX, double velY, ItemStack stack) {
        super(x, y, velX, velY, stack);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        double x = MathHelper.lerp(delta, prevX, this.x);
        double y = MathHelper.lerp(delta, prevY, this.y);
        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.translate(x, y, 500);
        double angle = Math.toDegrees(Math.atan2(velY, velX) * 0.5);
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) angle));
        context.drawItem(this.stack, -8, -8);
        context.drawItemInSlot(client.textRenderer, this.stack, -8, -8);
        matrixStack.pop();
    }
}
