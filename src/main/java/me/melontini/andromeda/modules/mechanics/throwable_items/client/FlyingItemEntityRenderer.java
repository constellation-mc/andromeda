package me.melontini.andromeda.modules.mechanics.throwable_items.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

public class FlyingItemEntityRenderer extends EntityRenderer<FlyingItemEntity> {

  private static final float MIN_DISTANCE = 12.25F;
  private final ItemRenderer itemRenderer;
  private final float scale;
  private final boolean lit;

  public FlyingItemEntityRenderer(EntityRendererProvider.Context ctx, float scale, boolean lit) {
    super(ctx);
    this.itemRenderer = ctx.getItemRenderer();
    this.scale = scale;
    this.lit = lit;
  }

  public FlyingItemEntityRenderer(EntityRendererProvider.Context context) {
    this(context, 1.0F, false);
  }

  @Override
  protected int getBlockLightLevel(FlyingItemEntity entity, BlockPos pos) {
    return this.lit ? 15 : super.getBlockLightLevel(entity, pos);
  }

  @Override
  public void render(
      FlyingItemEntity entity,
      float yaw,
      float tickDelta,
      PoseStack matrices,
      MultiBufferSource vertexConsumers,
      int light) {
    if (entity.tickCount >= 2
        || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < MIN_DISTANCE)) {
      matrices.pushPose();
      matrices.scale(this.scale, this.scale, this.scale);
      var quaternion = new Quaternionf(0, 0, 0, 1);
      quaternion =
          hamiltonProduct(quaternion, Axis.YP.rotationDegrees(entity.getViewYRot(tickDelta)));
      quaternion =
          hamiltonProduct(quaternion, Axis.XP.rotationDegrees(entity.getViewXRot(tickDelta)));
      matrices.mulPose(quaternion);
      this.itemRenderer.renderStatic(
          entity.getItem(),
          ItemDisplayContext.GROUND,
          light,
          OverlayTexture.NO_OVERLAY,
          matrices,
          vertexConsumers,
          entity.level(),
          entity.getId());
      matrices.popPose();
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
    return new Quaternionf(
        ax * bw + aw * bx + ay * bz - az * by,
        ay * bw + aw * by + az * bx - ax * bz,
        az * bw + aw * bz + ax * by - ay * bx,
        aw * bw - ax * bx - ay * by - az * bz);
  }

  @Override
  public ResourceLocation getTextureLocation(FlyingItemEntity entity) {
    return TextureAtlas.LOCATION_BLOCKS;
  }
}
