package dev.zenfyr.andromeda.common.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.config.handler.MultiConfigHandler;
import dev.zenfyr.pulsar.api.platform.Platform;
import java.util.function.Consumer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class AndromedaClient implements ClientModInitializer {

  private static AndromedaClient instance;

  public static final MultiConfigHandler CLIENT = new MultiConfigHandler(
      ModuleManager.get(),
      Platform.getPlatform().getConfigDir(),
      "client",
      RegisterConfigEvent.CLIENT);

  @Override
  public void onInitializeClient() {
    var manager = ModuleManager.get();
    Andromeda.get().onMergedEntryPoint(manager);
    instance = this;

    CLIENT.loadAll();
    CLIENT.saveAll();

    InitEvents.CLIENT.invoker().onModuleClientInit().runEntrypoint();
  }

  public static AndromedaClient get() {
    return instance;
  }

  public static void drawTexture(
      PoseStack matrices, int x, int y, Consumer<PoseStack> transform, ResourceLocation location) {
    RenderSystem.setShaderTexture(0, location);
    RenderSystem.setShader(GameRenderer::getPositionTexShader);

    matrices.pushPose();
    matrices.translate(x, y, 100);
    matrices.scale(1, 1, 1);
    transform.accept(matrices);

    Matrix4f matrix4f = matrices.last().pose();
    BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

    bufferBuilder.vertex(matrix4f, -8, 8, 0).uv(0, 1).endVertex();
    bufferBuilder.vertex(matrix4f, 8, 8, 0).uv(1, 1).endVertex();
    bufferBuilder.vertex(matrix4f, 8, -8, 0).uv(1, 0).endVertex();
    bufferBuilder.vertex(matrix4f, -8, -8, 0).uv(0, 0).endVertex();

    RenderSystem.enableBlend();
    BufferUploader.drawWithShader(bufferBuilder.end());
    RenderSystem.disableBlend();
    matrices.popPose();
  }
}
