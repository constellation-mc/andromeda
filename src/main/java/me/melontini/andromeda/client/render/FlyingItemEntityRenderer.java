package me.melontini.andromeda.client.render;

import me.melontini.andromeda.entity.FlyingItemEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

public class FlyingItemEntityRenderer extends EntityRenderer<FlyingItemEntity> {

    private static final float MIN_DISTANCE = 12.25F;
    private final ItemRenderer itemRenderer;
    private final float scale;
    private final boolean lit;

    public FlyingItemEntityRenderer(EntityRendererFactory.Context ctx, float scale, boolean lit) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
        this.scale = scale;
        this.lit = lit;
    }

    public FlyingItemEntityRenderer(EntityRendererFactory.Context context) {
        this(context, 1.0F, false);
    }

    @Override
    protected int getBlockLight(FlyingItemEntity entity, BlockPos pos) {
        return this.lit ? 15 : super.getBlockLight(entity, pos);
    }

    @Override
    public void render(FlyingItemEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (entity.age >= 2 || !(this.dispatcher.camera.getFocusedEntity().squaredDistanceTo(entity) < MIN_DISTANCE)) {
            matrices.push();
            matrices.scale(this.scale, this.scale, this.scale);
            var quaternion = new Quaternionf(0, 0, 0, 1);
            quaternion = hamiltonProduct(quaternion, RotationAxis.POSITIVE_Y.rotationDegrees(entity.getYaw(tickDelta)));
            quaternion = hamiltonProduct(quaternion, RotationAxis.POSITIVE_X.rotationDegrees(entity.getPitch(tickDelta)));
            matrices.multiply(quaternion);
            this.itemRenderer.renderItem(entity.getStack(), ModelTransformation.Mode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getId());
            matrices.pop();
            super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        }
    }

    private static Quaternionf hamiltonProduct(Quaternionf a, Quaternionf b) {
        float ax = a.x;
        float ay = a.y;
        float az = a.z;
        float aw = a.w;
        float bx = b.x;
        float by = b.y;
        float bz = b.z;
        float bw = b.w;
        return new Quaternionf(ax * bw + aw * bx + ay * bz - az * by, ay * bw + aw * by + az * bx - ax * bz, az * bw + aw * bz + ax * by - ay * bx, aw * bw - ax * bx - ay * by - az * bz);
    }

    @Override
    public Identifier getTexture(FlyingItemEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}
