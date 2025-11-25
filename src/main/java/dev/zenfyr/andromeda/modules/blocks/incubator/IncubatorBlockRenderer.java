package dev.zenfyr.andromeda.modules.blocks.incubator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class IncubatorBlockRenderer implements BlockEntityRenderer<IncubatorBlockEntity> {

  public IncubatorBlockRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public void render(
      IncubatorBlockEntity entity,
      float tickDelta,
      PoseStack matrices,
      MultiBufferSource vertexConsumers,
      int light,
      int overlay) {
    renderHay(matrices, vertexConsumers, light, overlay);
    renderItem(entity, matrices, vertexConsumers, light, overlay);
  }

  private void renderItem(
      IncubatorBlockEntity entity,
      PoseStack matrices,
      MultiBufferSource vertexConsumers,
      int light,
      int overlay) {
    matrices.pushPose();
    matrices.translate(0.5, 0.7, 0.5);
    Level world = MakeSure.notNull(entity.getLevel());
    BlockState state = world.getBlockState(entity.getBlockPos());
    if (state.getBlock() instanceof IncubatorBlock) {
      switch (state.getValue(IncubatorBlock.FACING)) {
        case NORTH -> matrices.mulPose(Axis.YP.rotationDegrees(180));
        case WEST -> matrices.mulPose(Axis.YP.rotationDegrees(270));
        case EAST -> matrices.mulPose(Axis.YP.rotationDegrees(90));
        case SOUTH -> matrices.mulPose(Axis.YP.rotationDegrees(0));
        default -> throw new IllegalStateException(
            String.valueOf(state.getValue(IncubatorBlock.FACING)));
      }
      matrices.mulPose(Axis.XP.rotationDegrees(-45));
      if (entity.processingTime > -1 && !entity.inventory.get(0).isEmpty()) {
        Minecraft.getInstance()
            .getItemRenderer()
            .renderStatic(
                entity.inventory.get(0),
                ItemDisplayContext.GROUND,
                light,
                overlay,
                matrices,
                vertexConsumers,
                entity.getLevel(),
                0);
      }
    }
    matrices.popPose();
  }

  private void renderHay(
      PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
    matrices.pushPose();
    matrices.scale(0.5F, 0.5F, 0.5F);
    matrices.translate(0.5, 1.4, 0.5);
    Minecraft.getInstance()
        .getBlockRenderer()
        .renderSingleBlock(
            Blocks.HORN_CORAL_FAN.defaultBlockState(/*very comfy*/ ),
            matrices,
            vertexConsumers,
            light,
            overlay);
    matrices.popPose();
  }

  public static void onClient() {
    if (IncubatorBlock.INCUBATOR_BLOCK.isPresent()) {
      BlockRenderLayerMap.INSTANCE.putBlocks(
          RenderType.cutout(), IncubatorBlock.INCUBATOR_BLOCK.get());
      // This is registered alongside the block, so no check is necessary
      BlockEntityRenderers.register(
          IncubatorBlock.INCUBATOR_BLOCK_ENTITY.get(), IncubatorBlockRenderer::new);
    }
  }
}
