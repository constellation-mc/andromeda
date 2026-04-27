package dev.zenfyr.andromeda.modules.gui.gui_particles;

import dev.zenfyr.pulsar.client.particles.ItemStackParticle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public class CustomItemStackParticle extends ItemStackParticle {
  public CustomItemStackParticle(double x, double y, double velX, double velY, ItemStack stack) {
    super(x, y, velX, velY, stack);
  }

  @Override
  public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
    double x = Mth.lerp(delta, prevX, this.x);
    double y = Mth.lerp(delta, prevY, this.y);
    Matrix3x2fStack matrixStack = context.pose();
    matrixStack.pushMatrix();
    // matrixStack.translate(x, y);
    double angle = Math.toDegrees(Math.atan2(velY, velX) * 0.5);
    // matrixStack.mul(Axis.ZP.rotationDegrees((float) angle));
    matrixStack.rotate((float) angle);
    context.renderItem(this.stack, -8, -8);
    context.renderItemDecorations(client.font, this.stack, -8, -8);
    matrixStack.popMatrix();
  }
}
