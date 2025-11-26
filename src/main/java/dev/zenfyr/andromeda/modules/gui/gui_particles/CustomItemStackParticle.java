package dev.zenfyr.andromeda.modules.gui.gui_particles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.zenfyr.pulsar.client.particles.ItemStackParticle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class CustomItemStackParticle extends ItemStackParticle {
  public CustomItemStackParticle(double x, double y, double velX, double velY, ItemStack stack) {
    super(x, y, velX, velY, stack);
  }

  @Override
  public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
    double x = Mth.lerp(delta, prevX, this.x);
    double y = Mth.lerp(delta, prevY, this.y);
    PoseStack matrixStack = context.pose();
    matrixStack.pushPose();
    matrixStack.translate(x, y, 500);
    double angle = Math.toDegrees(Math.atan2(velY, velX) * 0.5);
    matrixStack.mulPose(Axis.ZP.rotationDegrees((float) angle));
    context.renderItem(this.stack, -8, -8);
    context.renderItemDecorations(client.font, this.stack, -8, -8);
    matrixStack.popPose();
  }
}
