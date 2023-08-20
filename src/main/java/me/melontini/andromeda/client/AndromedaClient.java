package me.melontini.andromeda.client;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.client.particles.KnockoffTotemParticle;
import me.melontini.andromeda.client.render.BoatWithBlockRenderer;
import me.melontini.andromeda.client.render.FlyingItemEntityRenderer;
import me.melontini.andromeda.client.render.block.IncubatorBlockRenderer;
import me.melontini.andromeda.client.screens.FletchingScreen;
import me.melontini.andromeda.client.screens.MerchantInventoryScreen;
import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.config.AndromedaFeatureManager;
import me.melontini.andromeda.networks.ClientSideNetworking;
import me.melontini.andromeda.registries.BlockRegistry;
import me.melontini.andromeda.registries.EntityTypeRegistry;
import me.melontini.andromeda.registries.ScreenHandlerRegistry;
import me.melontini.andromeda.util.*;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.andromeda.util.translations.TranslationUpdater;
import me.melontini.dark_matter.api.analytics.MessageHandler;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import me.melontini.dark_matter.api.minecraft.client.util.DrawUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.RenderLayer;
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

import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static me.melontini.andromeda.util.SharedConstants.MODID;

@Environment(EnvType.CLIENT)
public class AndromedaClient implements ClientModInitializer {
    public static final Identifier WIKI_BUTTON_TEXTURE = new Identifier(MODID, "textures/gui/wiki_button.png");
    public static final Style WIKI_LINK = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://andromeda-wiki.pages.dev/"));

    public static String DEBUG_SPLASH;
    private static ItemStack frameStack = ItemStack.EMPTY;
    private float tooltipFlow;
    private float oldTooltipFlow;

    @Override
    public void onInitializeClient() {
        try {
            initClient();
        } catch (Exception e) {
            String cause = CauseFinder.findCause(e);
            if ("andromeda".equalsIgnoreCase(cause)) {
                throw new AndromedaException(true, "Failed to initialize Andromeda. Please report this to: " + FabricLoader.getInstance().getModContainer("andromeda").orElseThrow().getMetadata().getContact().get("issues"), e);
            } else {
                Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(cause);
                if (mod.isPresent()) {
                    if (mod.get().getMetadata().getContact().asMap().containsKey("issues")) {
                        throw new AndromedaException(false, "Failed to initialize Andromeda due to errors provided by: " + cause + ".\n Please report this to: " + mod.get().getMetadata().getContact().get("issues"), e);
                    }
                    throw new AndromedaException(false, "Failed to initialize Andromeda due to errors provided by:" + cause, e);
                }
            }
            throw new AndromedaException(true, "Failed to initialize Andromeda.", e);
        }
    }

    private void initClient() {
        GuiRegistry registry = AutoConfig.getGuiRegistry(AndromedaConfig.class);
        registry.registerPredicateTransformer((list, s, field, o, o1, guiRegistryAccess) ->
                list.stream().peek(gui -> {
                    gui.setRequirement(() -> !AndromedaFeatureManager.isModified(field));
                    if (gui instanceof TooltipListEntry<?> tooltipGui) {
                        if ("mod_json".equals(AndromedaFeatureManager.blameProcessor(field))) {
                            Text[] manager = new Text[]{TextUtil.translatable("andromeda.config.tooltip.manager.mod_json", Arrays.toString(AndromedaFeatureManager.blameMod(field)))};
                            tooltipGui.setTooltipSupplier(() -> Optional.of(manager));
                        } else {
                            Text[] manager = new Text[]{TextUtil.translatable("andromeda.config.tooltip.manager." + AndromedaFeatureManager.blameProcessor(field))};
                            tooltipGui.setTooltipSupplier(() -> Optional.of(manager));
                        }
                    }
                }).toList(), AndromedaFeatureManager::isModified);

        registry.registerPredicateTransformer((list, s, field, o, o1, guiRegistryAccess) -> {
            list.forEach(gui -> gui.setRequiresRestart(true));
            return list;
        }, field -> Andromeda.CONFIG.compatMode);

        if (Andromeda.CONFIG.autoUpdateTranslations) {
            boolean shouldUpdate = true;
            if (Files.exists(TranslationUpdater.LANG_PATH.resolve("en_us.json"))) {
                try {
                    FileTime lastModifiedTime = Files.getLastModifiedTime(TranslationUpdater.LANG_PATH.resolve("en_us.json"));
                    shouldUpdate = ChronoUnit.HOURS.between(lastModifiedTime.toInstant(), Instant.now()) >= 24;
                } catch (Exception ignored) {}
            }
            if (!shouldUpdate) shouldUpdate = SharedConstants.MOD_UPDATED;

            if (shouldUpdate) {
                Set<String> languages = Sets.newHashSet("en_us");
                String s = TranslationUpdater.getSelectedLanguage();
                if (!s.isEmpty()) languages.add(s);
                MessageHandler.EXECUTOR.submit(() -> TranslationUpdater.downloadTranslations(languages));
            } else {
                AndromedaLog.info("Skipped translations update.");
            }
        }
        ClientSideNetworking.register();
        registerEntityRenderers();
        registerBlockRenderers();

        inGameTooltips();

        ScreenEvents.BEFORE_INIT.register((client, screen1, scaledWidth, scaledHeight) -> {
            if (screen1 instanceof AbstractFurnaceScreen<?> abstractFurnaceScreen && Andromeda.CONFIG.guiParticles.furnaceScreenParticles) {
                ScreenEvents.afterTick(abstractFurnaceScreen).register(screen -> {
                    AbstractFurnaceScreen<?> furnaceScreen = (AbstractFurnaceScreen<?>) screen;
                    if (furnaceScreen.getScreenHandler().isBurning() && Utilities.RANDOM.nextInt(10) == 0) {
                        ScreenParticleHelper.addScreenParticle(screen, ParticleTypes.FLAME,
                                MathStuff.nextDouble(Utilities.RANDOM, furnaceScreen.x + 56, furnaceScreen.x + 56 + 14),
                                furnaceScreen.y + 36 + 13, MathStuff.nextDouble(Utilities.RANDOM, -0.01, 0.01),
                                0.05);
                    }
                });
            }
        });

        if (Andromeda.CONFIG.usefulFletching)
            HandledScreens.register(ScreenHandlerRegistry.FLETCHING_SCREEN_HANDLER, FletchingScreen::new);

        HandledScreens.register(ScreenHandlerRegistry.MERCHANT_INVENTORY_SCREEN_HANDLER, MerchantInventoryScreen::new);

        ParticleFactoryRegistry.getInstance().register(Andromeda.KNOCKOFF_TOTEM_PARTICLE, KnockoffTotemParticle.Factory::new);

        FabricLoader.getInstance().getModContainer(MODID).ifPresent(modContainer -> {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier(MODID, "dark"), modContainer, ResourcePackActivationType.NORMAL);
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (Andromeda.CONFIG.itemFrameTooltips) {
                var cast = client.crosshairTarget;
                getCast(cast);
                oldTooltipFlow = tooltipFlow;
                tooltipFlow = !frameStack.isEmpty() ? MathHelper.lerp(0.25f, tooltipFlow, 1) :
                        MathHelper.lerp(0.1f, tooltipFlow, 0);
            }
        });

        AndromedaReporter.handleUpload();
    }

    private void inGameTooltips() {
        HudRenderCallback.EVENT.register((matrices, delta) -> {
            if (Andromeda.CONFIG.itemFrameTooltips && MinecraftClient.getInstance().currentScreen == null) {
                var client = MinecraftClient.getInstance();

                if (!frameStack.isEmpty()) {
                    float flow = MathHelper.lerp(client.getTickDelta(), oldTooltipFlow, tooltipFlow);
                    matrices.push();
                    matrices.translate(0, 0, -450);
                    matrices.scale(1, 1, 1);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.setShaderColor(1, 1, 1, Math.min(flow, 0.8f));
                    var list = DrawUtil.FAKE_SCREEN.getTooltipFromItem(frameStack);
                    list.add(AndromedaTexts.ITEM_IN_FRAME);
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
        if (Andromeda.CONFIG.unknown)
            BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BlockRegistry.ROSE_OF_THE_VALLEY);

        if (Andromeda.CONFIG.incubatorSettings.enableIncubator) {
            BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BlockRegistry.INCUBATOR_BLOCK);
            BlockEntityRendererRegistry.register(BlockRegistry.INCUBATOR_BLOCK_ENTITY, IncubatorBlockRenderer::new);
        }
    }

    public void registerEntityRenderers() {
        if (Andromeda.CONFIG.newBoats.isFurnaceBoatOn)
            EntityRendererRegistry.register(EntityTypeRegistry.BOAT_WITH_FURNACE, ctx -> new BoatWithBlockRenderer(ctx, Blocks.FURNACE.getDefaultState().with(FurnaceBlock.FACING, Direction.NORTH)));
        if (Andromeda.CONFIG.newBoats.isJukeboxBoatOn)
            EntityRendererRegistry.register(EntityTypeRegistry.BOAT_WITH_JUKEBOX, ctx -> new BoatWithBlockRenderer(ctx, Blocks.JUKEBOX.getDefaultState()));
        if (Andromeda.CONFIG.newBoats.isTNTBoatOn)
            EntityRendererRegistry.register(EntityTypeRegistry.BOAT_WITH_TNT, ctx -> new BoatWithBlockRenderer(ctx, Blocks.TNT.getDefaultState()));
        if (Andromeda.CONFIG.newBoats.isHopperBoatOn)
            EntityRendererRegistry.register(EntityTypeRegistry.BOAT_WITH_HOPPER, ctx -> new BoatWithBlockRenderer(ctx, Blocks.HOPPER.getDefaultState()));

        if (Andromeda.CONFIG.newMinecarts.isAnvilMinecartOn)
            EntityRendererRegistry.register(EntityTypeRegistry.ANVIL_MINECART_ENTITY, ctx -> new MinecartEntityRenderer<>(ctx, EntityModelLayers.MINECART));
        if (Andromeda.CONFIG.newMinecarts.isNoteBlockMinecartOn)
            EntityRendererRegistry.register(EntityTypeRegistry.NOTEBLOCK_MINECART_ENTITY, ctx -> new MinecartEntityRenderer<>(ctx, EntityModelLayers.MINECART));
        if (Andromeda.CONFIG.newMinecarts.isJukeboxMinecartOn)
            EntityRendererRegistry.register(EntityTypeRegistry.JUKEBOX_MINECART_ENTITY, (ctx -> new MinecartEntityRenderer<>(ctx, EntityModelLayers.MINECART)));

        EntityRendererRegistry.register(EntityTypeRegistry.FLYING_ITEM, FlyingItemEntityRenderer::new);
    }
}
