package me.melontini.andromeda.modules.blocks.incubator.client;

import me.melontini.andromeda.modules.blocks.incubator.IncubatorBlock;
import me.melontini.andromeda.modules.blocks.incubator.IncubatorBlockEntity;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class IncubatorBlockRenderer implements BlockEntityRenderer<IncubatorBlockEntity> {

    public IncubatorBlockRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(IncubatorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        renderHay(matrices, vertexConsumers, light, overlay);
        renderItem(entity, matrices, vertexConsumers, light, overlay);
    }

    private void renderItem(IncubatorBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5, 0.7, 0.5);
        World world = MakeSure.notNull(entity.getWorld());
        BlockState state = world.getBlockState(entity.getPos());
        if (state.getBlock() instanceof IncubatorBlock) {
            switch (state.get(IncubatorBlock.FACING)) {
                case NORTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                case WEST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270));
                case EAST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                case SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));
            }
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-45));
            if (entity.processingTime > -1 && !entity.inventory.get(0).isEmpty()) {
                MinecraftClient.getInstance().getItemRenderer().renderItem(entity.inventory.get(0), ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
            }
        }
        matrices.pop();
    }

    private void renderHay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.scale(0.5F, 0.5F, 0.5F);
        matrices.translate(0.5, 1.4, 0.5);
        MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(Blocks.HORN_CORAL_FAN.getDefaultState(/*very comfy*/), matrices, vertexConsumers, light, overlay);
        matrices.pop();
    }
}
