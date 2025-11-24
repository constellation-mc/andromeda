package me.melontini.andromeda.modules.mechanics.throwable_items.client;

import com.mojang.blaze3d.vertex.PoseStack;
import me.melontini.dark_matter.api.glitter.particles.AbstractScreenParticle;
import me.melontini.dark_matter.api.minecraft.client.util.DrawUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class DyeParticle extends AbstractScreenParticle {

  private final ItemStack stack;
  private float scale = 0.1F;
  private float oldScale = 0.1F;
  private float offset = 1;
  private float oldOffset = 1;

  public DyeParticle(double x, double y, double velX, double velY, ItemStack stack) {
    super(x, y, velX, velY);
    this.stack = stack;
  }

  @Override
  public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
    PoseStack matrices = context.pose();
    float scale = Mth.lerp(delta, oldScale, this.scale);
    float offset = Mth.lerp(delta, oldOffset, this.offset);
    matrices.pushPose();
    matrices.translate(x, y + offset, 500);
    matrices.scale(scale, scale, 1);
    BakedModel model = client.getItemRenderer().getModel(stack, null, null, 0);
    DrawUtil.renderGuiItemModelCustomMatrix(matrices, stack, -8, -8, model);
    matrices.popPose();
  }

  @Override
  protected void tick() {
    int window =
        Math.max(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
    oldScale = scale;
    scale = Mth.lerp(0.15f, scale, window / 25f);
    if (scale > (window / 25f) * 0.99f) {
      oldOffset = offset;
      offset += 1 * (offset * 0.07f);
    }
  }
}
