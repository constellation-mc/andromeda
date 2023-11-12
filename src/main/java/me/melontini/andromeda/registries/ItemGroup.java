package me.melontini.andromeda.registries;

import com.mojang.blaze3d.systems.RenderSystem;
import me.melontini.andromeda.util.AndromedaTexts;
import me.melontini.dark_matter.api.content.ContentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;

import java.util.function.Consumer;

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.registries.Common.start;

public class ItemGroup {

    private static boolean animate = true;
    @SuppressWarnings("unused")
    public static final Keeper<net.minecraft.item.ItemGroup> GROUP = start(() -> ContentBuilder.ItemGroupBuilder.create(id("group"))
            .entries(entries -> Registry.ITEM.streamEntries()
                    .filter(ref -> ref.getKey().map(k -> k.getValue().getNamespace().equals("andromeda")).orElse(false))
                    .forEach(ref -> entries.add(ref.value())))
            .animatedIcon(() -> (group, matrices, itemX, itemY, selected, isTopRow) -> {
                try {
                    if (!animate) return;
                    MinecraftClient client = MinecraftClient.getInstance();

                    drawTexture(matrices, itemX + 8, itemY + 8, stack -> {}, new Identifier("andromeda:textures/gui/background.png"));
                    drawTexture(matrices, itemX + 8, itemY + 8, stack -> stack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(Util.getMeasuringTimeMs() * 0.05f)),
                            new Identifier("andromeda:textures/gui/galaxy.png"));
                } catch (Throwable t) {
                    animate = false;
                }
            }).displayName(AndromedaTexts.ITEM_GROUP_NAME));

    public static void drawTexture(MatrixStack matrices, int x, int y, Consumer<MatrixStack> transform, Identifier id) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, id);

        matrices.push();
        matrices.translate(x, y, 100);
        matrices.scale(1, 1, 1);
        transform.accept(matrices);

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        bufferBuilder.vertex(matrix4f, -8, 8, 0).texture(0, 1).next();
        bufferBuilder.vertex(matrix4f, 8, 8, 0).texture(1, 1).next();
        bufferBuilder.vertex(matrix4f, 8, -8, 0).texture(1, 0).next();
        bufferBuilder.vertex(matrix4f, -8, -8, 0).texture(0, 0).next();

        //RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        BufferRenderer.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
        //RenderSystem.disableDepthTest();
        matrices.pop();
    }
}
