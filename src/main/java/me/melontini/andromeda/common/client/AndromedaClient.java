package me.melontini.andromeda.common.client;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.CustomLog;
import lombok.Getter;
import me.melontini.andromeda.base.AndromedaConfig;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.events.BlockadesEvent;
import me.melontini.andromeda.base.util.Promise;
import me.melontini.andromeda.common.client.config.AutoConfigScreen;
import me.melontini.andromeda.common.client.config.FeatureBlockade;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.base.util.Support;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static me.melontini.andromeda.common.registries.Common.id;

@CustomLog
@Getter
@Environment(EnvType.CLIENT)
public class AndromedaClient {

    private static AndromedaClient INSTANCE;
    private boolean animate = true;

    public static void init() {
        INSTANCE = new AndromedaClient();
        INSTANCE.onInitializeClient(ModuleManager.get());
        FabricLoader.getInstance().getObjectShare().put("andromeda:client", INSTANCE);
    }

    public void onInitializeClient(ModuleManager manager) {
        Support.run("cloth-config", () -> AutoConfigScreen::register);
        if (!AndromedaConfig.get().sideOnlyMode) ClientSideNetworking.register();
        else {
            manager.all().stream().map(Promise::get).forEach(module -> {
                switch (module.meta().environment()) {
                    case ANY, CLIENT -> {
                    }
                    default -> FeatureBlockade.get().explain(module, "enabled", () -> true,
                            "andromeda.config.option_manager.reason.andromeda.side_only_enabled");
                }
            });
        }
        BlockadesEvent.BUS.invoker().explain(FeatureBlockade.get());

        ResourceManagerHelper.registerBuiltinResourcePack(id("dark"), CommonValues.mod(), ResourcePackActivationType.NORMAL);
        AndromedaItemGroup.GROUP.dm$setIconAnimation((group, context, itemX, itemY, selected, isTopRow) -> {
            try {
                if (!animate) return;
                drawTexture(context.getMatrices(), itemX + 8, itemY + 8, stack -> {
                }, new Identifier("andromeda:textures/gui/background.png"));
                drawTexture(context.getMatrices(), itemX + 8, itemY + 8, stack -> stack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(Util.getMeasuringTimeMs() * 0.05f)),
                        new Identifier("andromeda:textures/gui/galaxy.png"));
            } catch (Throwable t) {
                animate = false;
            }
        });
    }

    private void printMissingTooltips(ModuleManager manager) {
        Set<String> missing = new LinkedHashSet<>();
        for (Promise<?> module : manager.all()) {
            String m = "config.andromeda.%s.@Tooltip".formatted(module.meta().dotted());
            if (!I18n.hasTranslation(m)) missing.add(m);

            Arrays.stream(manager.getConfigClass(module.getClass()).getFields())
                    .filter(f -> !"enabled".equals(f.getName()) && !f.isAnnotationPresent(ConfigEntry.Gui.Excluded.class))
                    .map(field -> "config.andromeda.%s.option.%s.@Tooltip".formatted(module.meta().dotted(), field.getName()))
                    .filter(I18n::hasTranslation).forEach(missing::add);
        }
        StringBuilder b = new StringBuilder();
        missing.forEach(s -> b.append('\t').append(s).append('\n'));
        LOGGER.info("Missing tooltips:\n{}", b);
    }

    public void lateInit(ModuleManager manager) {
        if (Debug.Keys.PRINT_MISSING_TOOLTIPS.isPresent()) printMissingTooltips(manager);
    }

    @Override
    public String toString() {
        return "AndromedaClient{version=" + CommonValues.version() + "}";
    }

    public static AndromedaClient get() {
        return Objects.requireNonNull(INSTANCE, "AndromedaClient not initialized");
    }

    public static void drawTexture(MatrixStack matrices, int x, int y, Consumer<MatrixStack> transform, Identifier id) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
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
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
        //RenderSystem.disableDepthTest();
        matrices.pop();
    }
}
