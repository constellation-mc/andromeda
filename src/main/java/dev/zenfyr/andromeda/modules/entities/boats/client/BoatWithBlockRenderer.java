package dev.zenfyr.andromeda.modules.entities.boats.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Vector2f;

public class BoatWithBlockRenderer extends BoatRenderer {

  public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
  private final BlockState blockState;
  private final Vector2f offset;
  private final BlockModelResolver blockModelResolver;

  public BoatWithBlockRenderer(
      EntityRendererProvider.Context context,
      BlockState blockState,
      Vector2f offset,
      ModelLayerLocation modelLayer) {
    super(context, modelLayer);
    this.blockState = blockState;
    this.offset = offset;
    this.blockModelResolver = context.getBlockModelResolver();
  }

  @Override
  public void submit(
      BoatRenderState boatRenderState,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      CameraRenderState cameraRenderState) {
    super.submit(boatRenderState, poseStack, submitNodeCollector, cameraRenderState);
    BlockModelRenderState displayBlockModel =
        ((RenderStateDuck) boatRenderState).andromeda$blockRenderState();
    if (blockState == null || displayBlockModel.isEmpty()) return;

    poseStack.pushPose();
    // poseStack.translate(0.0F, 0.375F, 0.0F);
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - boatRenderState.yRot));
    float f = boatRenderState.hurtTime;
    if (f > 0.0F) {
      poseStack.mulPose(Axis.XP.rotationDegrees(
          Mth.sin(f) * f * boatRenderState.damageTime / 10.0F * boatRenderState.hurtDir));
    }

    if (!boatRenderState.isUnderWater && !Mth.equal(boatRenderState.bubbleAngle, 0.0F)) {
      poseStack.mulPose(new Quaternionf()
          .setAngleAxis(boatRenderState.bubbleAngle * (float) (Math.PI / 180.0), 1.0F, 0.0F, 1.0F));
    }

    poseStack.scale(0.8f, 0.8f, 0.8f);
    poseStack.translate(0.5f, offset.x(), offset.y());
    poseStack.mulPose(Axis.YP.rotationDegrees(-180.0f));

    displayBlockModel.submit(
        poseStack,
        submitNodeCollector,
        boatRenderState.lightCoords,
        OverlayTexture.NO_OVERLAY,
        boatRenderState.outlineColor);

    poseStack.popPose();
  }

  @Override
  public void extractRenderState(AbstractBoat entity, BoatRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    this.blockModelResolver.update(
        ((RenderStateDuck) state).andromeda$blockRenderState(),
        this.blockState,
        BLOCK_DISPLAY_CONTEXT);
  }
}
