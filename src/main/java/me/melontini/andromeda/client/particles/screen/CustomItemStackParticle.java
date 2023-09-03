package me.melontini.andromeda.client.particles.screen;

import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.glitter.particles.ItemStackParticle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class CustomItemStackParticle extends ItemStackParticle {
    private final int seed;
    public CustomItemStackParticle(double x, double y, double velX, double velY, ItemStack stack) {
        super(x, y, velX, velY, stack);
        this.seed = MathStuff.threadRandom().nextInt();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float x = (float) MathHelper.lerp(delta, prevX, this.x);
        float y = (float) MathHelper.lerp(delta, prevY, this.y);
        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.translate(x, y, 500);
        float angle = (float) Math.toDegrees(Math.atan2(velY, velX) * 0.5);
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
        context.drawItem(this.stack, -8, -8);
        context.drawItemInSlot(client.textRenderer, this.stack, -8, -8);
        matrixStack.pop();
    }
}
