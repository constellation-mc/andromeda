package me.melontini.andromeda.client.particles.screen;

import me.melontini.dark_matter.api.glitter.particles.AbstractScreenParticle;
import me.melontini.dark_matter.api.minecraft.client.util.DrawUtil;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        float scale = MathHelper.lerp(delta, oldScale, this.scale);
        float offset = MathHelper.lerp(delta, oldOffset, this.offset);
        matrices.push();
        matrices.translate(x, y + offset, 500);
        matrices.scale(scale, scale, 1);
        BakedModel model = client.getItemRenderer().getModel(stack, null, null, 0);
        DrawUtil.renderGuiItemModelCustomMatrix(matrices, stack, -8, -8, model);
        matrices.pop();
    }

    @Override
    protected void tick() {
        int window = Math.max(client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        oldScale = scale;
        scale = MathHelper.lerp(0.15f, scale, window / 25f);
        if (scale > (window / 25f) * 0.99f) {
            oldOffset = offset;
            offset += 1 * (offset * 0.07f);
        }
    }
}
