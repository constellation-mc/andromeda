package me.melontini.andromeda.modules.entities.boats.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.client.render.entity.model.RaftEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

public class BoatWithBlockRenderer extends BoatEntityRenderer {

    private final BlockState blockState;

    public BoatWithBlockRenderer(EntityRendererFactory.Context context, BlockState blockState) {
        super(context, false);
        this.blockState = blockState;
    }

    @Override
    public void render(BoatEntity boatEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(boatEntity, f, g, matrixStack, vertexConsumerProvider, i);
        if (blockState != null) if (blockState.getRenderType() != BlockRenderType.INVISIBLE) {
            matrixStack.push();
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - f));
            float h = (float) boatEntity.getDamageWobbleTicks() - g;
            float j = boatEntity.getDamageWobbleStrength() - g;

            if (h > 0.0F) {
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(h) * h * j / 10.0F * (float) boatEntity.getDamageWobbleSide()));
            }

            float k = boatEntity.interpolateBubbleWobble(g);
            if (!MathHelper.approximatelyEquals(k, 0.0F)) {
                matrixStack.multiply(new Quaternionf().setAngleAxis(boatEntity.interpolateBubbleWobble(g) * (float) (Math.PI / 180.0), 1.0F, 0.0F, 1.0F));
            }

            Pair<Identifier, CompositeEntityModel<BoatEntity>> pair = this.texturesAndModels.get(boatEntity.getVariant());
            matrixStack.scale(0.8F, 0.8F, 0.8F);
            matrixStack.translate(0.5, pair.getSecond() instanceof RaftEntityModel ? 0.7 : 0.25, pair.getSecond() instanceof RaftEntityModel ? 1.06 : 1);
            //:sob:
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-180));
            MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(blockState, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV);
            matrixStack.pop();
        }
    }
}
