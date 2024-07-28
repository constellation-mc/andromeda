package me.melontini.andromeda.common.client;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.CustomLog;
import lombok.Getter;
import me.melontini.andromeda.base.AndromedaConfig;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.events.BlockadesEvent;
import me.melontini.andromeda.base.util.Promise;
import me.melontini.andromeda.base.util.config.ConfigHandler;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.client.config.FeatureBlockade;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.item_group.ItemGroupAnimaton;
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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.melontini.andromeda.common.Andromeda.id;

@CustomLog
@Getter
@Environment(EnvType.CLIENT)
public final class AndromedaClient {

    public static final ConfigHandler HANDLER = new ConfigHandler(FabricLoader.getInstance().getConfigDir(), ConfigState.CLIENT, ModuleManager.get().all().stream().map(Promise::get).toList());
    private static final Identifier BACKGROUND_TEXTURE = Andromeda.id("textures/gui/background.png");
    private static final Identifier GALAXY_TEXTURE = Andromeda.id("textures/gui/galaxy.png");

    private static Supplier<AndromedaClient> INSTANCE = () -> {
        throw new NullPointerException("AndromedaClient not initialized");
    };
    private boolean animate = true;

    public static void init() {
        var instance = new AndromedaClient();
        instance.onInitializeClient(ModuleManager.get());
        FabricLoader.getInstance().getObjectShare().put("andromeda:client", instance);
        INSTANCE = () -> instance;
    }

    public void onInitializeClient(ModuleManager manager) {
        var blockade = FeatureBlockade.get();
        if (!AndromedaConfig.get().sideOnlyMode) ClientSideNetworking.register(manager);
        else {
            manager.all().stream().map(Promise::get).forEach(module -> {
                switch (module.meta().environment()) {
                    case ANY, CLIENT -> {
                    }
                    default -> blockade.explain(module, "enabled", (moduleManager) -> true,
                            blockade.andromeda("side_only_enabled"));
                }
            });
        }
        BlockadesEvent.BUS.invoker().explain(manager, blockade);

        ResourceManagerHelper.registerBuiltinResourcePack(id("dark"), CommonValues.mod(), ResourcePackActivationType.NORMAL);
        Andromeda.GROUP.ifPresent(g -> ItemGroupAnimaton.setIconAnimation(g, (group, context, itemX, itemY, selected, isTopRow) -> {
            try {
                if (!animate) return;
                drawTexture(context.getMatrices(), itemX + 8, itemY + 8, stack -> {
                }, BACKGROUND_TEXTURE);
                drawTexture(context.getMatrices(), itemX + 8, itemY + 8, stack -> stack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(Util.getMeasuringTimeMs() * 0.05f)),
                        GALAXY_TEXTURE);
            } catch (Throwable t) {
                animate = false;
            }
        }));
    }

    private void printMissingTooltips(ModuleManager manager) {
        Set<String> missing = new LinkedHashSet<>();
        for (Promise<?> module : manager.all()) {
            String m = "config.andromeda.%s.@Tooltip".formatted(module.meta().dotted());
            if (!I18n.hasTranslation(m)) missing.add(m);

            for (ConfigState value : ConfigState.values()) {
                var def = module.get().getConfigDefinition(value);
                if (def == null) continue;

                Arrays.stream(def.supplier().get().getFields())
                        .filter(f -> !f.isAnnotationPresent(ConfigEntry.Gui.Excluded.class))
                        .map(field -> "config.andromeda.%s.option.%s.@Tooltip".formatted(module.meta().dotted(), field.getName()))
                        .filter(I18n::hasTranslation).forEach(missing::add);
            }
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
        return INSTANCE.get();
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
