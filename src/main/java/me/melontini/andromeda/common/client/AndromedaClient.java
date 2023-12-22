package me.melontini.andromeda.common.client;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.common.client.config.AutoConfigScreen;
import me.melontini.andromeda.common.client.config.FeatureBlockade;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.CrashHandler;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import java.util.Objects;
import java.util.function.Consumer;

import static me.melontini.andromeda.common.registries.Common.id;
import static me.melontini.andromeda.util.CommonValues.MODID;

@Getter
@Environment(EnvType.CLIENT)
public class AndromedaClient {

    private static AndromedaClient INSTANCE;
    private static boolean animate = true;

    public static void init() {
        INSTANCE = new AndromedaClient();
        INSTANCE.onInitializeClient();
        FabricLoader.getInstance().getObjectShare().put("andromeda:client", INSTANCE);
    }

    public void onInitializeClient() {
        Support.run("cloth-config", () -> AutoConfigScreen::register);
        if (!Config.get().sideOnlyMode) ClientSideNetworking.register();
        else {
            for (Module<?> module : ModuleManager.get().all()) {
                switch (module.meta().environment()) {
                    case ANY, CLIENT -> {
                    }
                    default -> FeatureBlockade.get().explain(module, "enabled", () -> true,
                            TextUtil.translatable("andromeda.config.option_manager.reason.andromeda.side_only_enabled"));
                }
            }
        }
        for (Module<?> module : ModuleManager.get().all()) {
            module.collectBlockades();
        }

        FabricLoader.getInstance().getModContainer(MODID).ifPresent(mod ->
                ResourceManagerHelper.registerBuiltinResourcePack(id("dark"), mod, ResourcePackActivationType.NORMAL));
        AndromedaItemGroup.GROUP.ifPresent(group -> group.dm$setIconAnimation((group1, matrices, itemX, itemY, selected, isTopRow) -> {
            try {
                if (!animate) return;
                drawTexture(matrices, itemX + 8, itemY + 8, stack -> {
                }, new Identifier("andromeda:textures/gui/background.png"));
                drawTexture(matrices, itemX + 8, itemY + 8, stack -> stack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(Util.getMeasuringTimeMs() * 0.05f)),
                        new Identifier("andromeda:textures/gui/galaxy.png"));
            } catch (Throwable t) {
                animate = false;
            }
        }));

        Support.runWeak(EnvType.CLIENT, () -> CrashHandler::nukeProfile);
    }

    public void lateInit() {
    }

    @Override
    public String toString() {
        return "AndromedaClient{version=" + CommonValues.version() + "}";
    }

    public static AndromedaClient get() {
        return Objects.requireNonNull(INSTANCE, "AndromedaClient not initialized");
    }

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
