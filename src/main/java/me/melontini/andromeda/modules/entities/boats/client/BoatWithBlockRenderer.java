package me.melontini.andromeda.modules.entities.boats.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.RaftModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

public class BoatWithBlockRenderer extends BoatRenderer {

  private final BlockState blockState;

  public BoatWithBlockRenderer(EntityRendererProvider.Context context, BlockState blockState) {
    super(context, false);
    this.blockState = blockState;
  }

  @Override
  public void render(
      Boat boatEntity,
      float f,
      float g,
      PoseStack matrixStack,
      MultiBufferSource vertexConsumerProvider,
      int i) {
    super.render(boatEntity, f, g, matrixStack, vertexConsumerProvider, i);
    if (blockState != null)
      if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
        matrixStack.pushPose();
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
        float h = (float) boatEntity.getHurtTime() - g;
        float j = boatEntity.getDamage() - g;

        if (h > 0.0F) {
          matrixStack.mulPose(Axis.XP.rotationDegrees(
              Mth.sin(h) * h * j / 10.0F * (float) boatEntity.getHurtDir()));
        }

        float k = boatEntity.getBubbleAngle(g);
        if (!Mth.equal(k, 0.0F)) {
          matrixStack.mulPose(new Quaternionf()
              .setAngleAxis(
                  boatEntity.getBubbleAngle(g) * (float) (Math.PI / 180.0), 1.0F, 0.0F, 1.0F));
        }

        Pair<ResourceLocation, ListModel<Boat>> pair =
            this.boatResources.get(boatEntity.getVariant());
        matrixStack.scale(0.8F, 0.8F, 0.8F);
        matrixStack.translate(
            0.5,
            pair.getSecond() instanceof RaftModel ? 0.7 : 0.25,
            pair.getSecond() instanceof RaftModel ? 1.06 : 1);
        // :sob:
        matrixStack.mulPose(Axis.YP.rotationDegrees(-180));
        Minecraft.getInstance()
            .getBlockRenderer()
            .renderSingleBlock(
                blockState, matrixStack, vertexConsumerProvider, i, OverlayTexture.NO_OVERLAY);
        matrixStack.popPose();
      }
  }
}
