package me.melontini.andromeda.client.render.block;

import me.melontini.andromeda.blocks.IncubatorBlock;
import me.melontini.andromeda.blocks.entities.IncubatorBlockEntity;
import me.melontini.andromeda.registries.BlockRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;

import java.util.Objects;

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
        if (Objects.requireNonNull(entity.getWorld()).getBlockState(entity.getPos()).isOf(BlockRegistry.INCUBATOR_BLOCK)) {
            switch (entity.getWorld().getBlockState(entity.getPos()).get(IncubatorBlock.FACING)) {
                case NORTH -> matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
                case WEST -> matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(270));
                case EAST -> matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
                case SOUTH -> matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(0));
            }
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-45));
            if (entity.processingTime > -1 && !entity.inventory.get(0).isEmpty()) {
                MinecraftClient.getInstance().getItemRenderer().renderItem(entity.inventory.get(0), ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers, 0);
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
