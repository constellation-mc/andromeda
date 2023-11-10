package me.melontini.andromeda.client;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.client.config.AutoConfigScreen;
import me.melontini.andromeda.client.particles.KnockoffTotemParticle;
import me.melontini.andromeda.client.render.BoatWithBlockRenderer;
import me.melontini.andromeda.client.render.FlyingItemEntityRenderer;
import me.melontini.andromeda.client.render.block.IncubatorBlockRenderer;
import me.melontini.andromeda.client.screens.FletchingScreen;
import me.melontini.andromeda.client.screens.MerchantInventoryScreen;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.networks.ClientSideNetworking;
import me.melontini.andromeda.registries.BlockRegistry;
import me.melontini.andromeda.registries.EntityTypeRegistry;
import me.melontini.andromeda.registries.ScreenHandlerRegistry;
import me.melontini.andromeda.util.AndromedaReporter;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.translations.TranslationUpdater;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import me.melontini.dark_matter.api.minecraft.client.util.DrawUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static me.melontini.andromeda.util.CommonValues.MODID;

@Environment(EnvType.CLIENT)
public class AndromedaClient {

    private static AndromedaClient INSTANCE;

    @Getter
    private final Notifier notifier = new Notifier();

    public final Identifier WIKI_BUTTON_TEXTURE = new Identifier(MODID, "textures/gui/wiki_button.png");
    public final Style WIKI_LINK = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://andromeda-wiki.pages.dev/"));

    public String DEBUG_SPLASH;

    private ItemStack frameStack = ItemStack.EMPTY;
    private float tooltipFlow;
    private float oldTooltipFlow;

    public static void init() {
        INSTANCE = new AndromedaClient();
        INSTANCE.onInitializeClient();
        FabricLoader.getInstance().getObjectShare().put("andromeda:client", INSTANCE);
    }

    public void onInitializeClient() {
        Support.run("cloth-config", () -> AutoConfigScreen::register);
        if (Config.get().autoUpdateTranslations) TranslationUpdater.checkAndUpdate();
        ClientSideNetworking.register();
        registerEntityRenderers();
        registerBlockRenderers();

        inGameTooltips();

        ScreenEvents.BEFORE_INIT.register((client, screen1, scaledWidth, scaledHeight) -> {
            if (screen1 instanceof AbstractFurnaceScreen<?> abstractFurnaceScreen && Config.get().guiParticles.furnaceScreenParticles) {
                ScreenEvents.afterTick(abstractFurnaceScreen).register(screen -> {
                    AbstractFurnaceScreen<?> furnaceScreen = (AbstractFurnaceScreen<?>) screen;
                    if (furnaceScreen.getScreenHandler().isBurning() && MathStuff.threadRandom().nextInt(10) == 0) {
                        ScreenParticleHelper.addScreenParticle(screen, ParticleTypes.FLAME,
                                MathStuff.nextDouble(furnaceScreen.x + 56, furnaceScreen.x + 56 + 14),
                                furnaceScreen.y + 36 + 13, MathStuff.nextDouble(-0.01, 0.01),
                                0.05);
                    }
                });
            }
        });

        ScreenHandlerRegistry.FLETCHING.ifPresent(s -> HandledScreens.register(s, FletchingScreen::new));
        ScreenHandlerRegistry.MERCHANT_INVENTORY.ifPresent(s -> HandledScreens.register(s, MerchantInventoryScreen::new));

        ParticleFactoryRegistry.getInstance().register(Andromeda.get().KNOCKOFF_TOTEM_PARTICLE, KnockoffTotemParticle.Factory::new);

        FabricLoader.getInstance().getModContainer(MODID).ifPresent(modContainer -> {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier(MODID, "dark"), modContainer, ResourcePackActivationType.NORMAL);
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (Config.get().itemFrameTooltips) {
                var cast = client.crosshairTarget;
                getCast(cast);
                oldTooltipFlow = tooltipFlow;
                tooltipFlow = !frameStack.isEmpty() ? MathHelper.lerp(0.25f, tooltipFlow, 1) :
                        MathHelper.lerp(0.1f, tooltipFlow, 0);
                if (Math.abs(tooltipFlow) < 1.0E-5F) tooltipFlow = 0;
            }
        });

        Support.runWeak(EnvType.CLIENT, () -> AndromedaReporter::handleUpload);
    }

    public void lateInit() {
        notifier.showQueued();
    }

    private void inGameTooltips() {
        HudRenderCallback.EVENT.register((matrices, delta) -> {
            if (Config.get().itemFrameTooltips && MinecraftClient.getInstance().currentScreen == null) {
                var client = MinecraftClient.getInstance();

                if (!frameStack.isEmpty()) {
                    float flow = MathHelper.lerp(client.getTickDelta(), oldTooltipFlow, tooltipFlow);
                    matrices.push();
                    matrices.translate(0, 0, -450);
                    matrices.scale(1, 1, 1);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.setShaderColor(1, 1, 1, Math.min(flow, 0.8f));
                    var list = DrawUtil.getFakeScreen().getTooltipFromItem(frameStack);
                    //list.add(AndromedaTexts.ITEM_IN_FRAME);
                    List<TooltipComponent> list1 = list.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());

                    frameStack.getTooltipData().ifPresent(datax -> list1.add(1, Utilities.supply(() -> {
                        TooltipComponent component = TooltipComponentCallback.EVENT.invoker().getComponent(datax);
                        if (component == null) component = TooltipComponent.of(datax);
                        return component;
                    })));

                    int j = 0;
                    for (TooltipComponent tooltipComponent : list1) {
                        j += tooltipComponent.getHeight();
                    }

                    DrawUtil.renderTooltipFromComponents(matrices, list1, ((client.getWindow().getScaledWidth() / 2f) - (flow * 15)) + 15, ((client.getWindow().getScaledHeight() - j) / 2f) + 12);
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    RenderSystem.disableBlend();
                    matrices.pop();
                }
            }
        });
    }

    private void getCast(HitResult cast) {
        if (cast != null) if (cast.getType() == HitResult.Type.ENTITY) {
            EntityHitResult hitResult = (EntityHitResult) cast;
            if (hitResult.getEntity() instanceof ItemFrameEntity itemFrameEntity) {
                frameStack = itemFrameEntity.getHeldItemStack();
                return;
            }
        }
        frameStack = ItemStack.EMPTY;
    }

    public void registerBlockRenderers() {
        BlockRegistry.ROSE_OF_THE_VALLEY.ifPresent(b -> BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), b));

        BlockRegistry.INCUBATOR_BLOCK.ifPresent(b -> BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), b));
        BlockRegistry.INCUBATOR_BLOCK_ENTITY.ifPresent(b -> BlockEntityRendererFactories.register(b, IncubatorBlockRenderer::new));
    }

    public void registerEntityRenderers() {
        EntityTypeRegistry.BOAT_WITH_FURNACE.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new BoatWithBlockRenderer(ctx, Blocks.FURNACE.getDefaultState().with(FurnaceBlock.FACING, Direction.NORTH))));
        EntityTypeRegistry.BOAT_WITH_JUKEBOX.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new BoatWithBlockRenderer(ctx, Blocks.JUKEBOX.getDefaultState())));
        EntityTypeRegistry.BOAT_WITH_TNT.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new BoatWithBlockRenderer(ctx, Blocks.TNT.getDefaultState())));
        EntityTypeRegistry.BOAT_WITH_HOPPER.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new BoatWithBlockRenderer(ctx, Blocks.HOPPER.getDefaultState())));

        EntityTypeRegistry.ANVIL_MINECART_ENTITY.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new MinecartEntityRenderer<>(ctx, EntityModelLayers.MINECART)));
        EntityTypeRegistry.NOTEBLOCK_MINECART_ENTITY.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new MinecartEntityRenderer<>(ctx, EntityModelLayers.MINECART)));
        EntityTypeRegistry.JUKEBOX_MINECART_ENTITY.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new MinecartEntityRenderer<>(ctx, EntityModelLayers.MINECART)));

        EntityTypeRegistry.FLYING_ITEM.ifPresent(e -> EntityRendererRegistry.register(e, FlyingItemEntityRenderer::new));
    }

    @Override
    public String toString() {
        return "AndromedaClient{version=" + CommonValues.version() + "}";
    }

    public static AndromedaClient get() {
        return Objects.requireNonNull(INSTANCE, "AndromedaClient not initialized");
    }
}
