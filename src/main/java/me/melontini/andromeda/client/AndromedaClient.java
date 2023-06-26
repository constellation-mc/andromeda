package me.melontini.andromeda.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.client.particles.KnockoffTotemParticle;
import me.melontini.andromeda.client.render.BoatWithBlockRenderer;
import me.melontini.andromeda.client.render.FlyingItemEntityRenderer;
import me.melontini.andromeda.client.render.block.IncubatorBlockRenderer;
import me.melontini.andromeda.client.screens.FletchingScreen;
import me.melontini.andromeda.mixin.gui.gui_particles.accessors.HandledScreenAccessor;
import me.melontini.andromeda.networks.ClientSideNetworking;
import me.melontini.andromeda.registries.BlockRegistry;
import me.melontini.andromeda.registries.EntityTypeRegistry;
import me.melontini.andromeda.util.AndromedaAnalytics;
import me.melontini.andromeda.util.AndromedaTexts;
import me.melontini.crackerutil.client.util.DrawUtil;
import me.melontini.crackerutil.client.util.ScreenParticleHelper;
import me.melontini.crackerutil.util.MathStuff;
import me.melontini.crackerutil.util.Utilities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
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
import java.util.stream.Collectors;

import static me.melontini.andromeda.Andromeda.MODID;

@Environment(EnvType.CLIENT)
public class AndromedaClient implements ClientModInitializer {
    public static final Identifier WIKI_BUTTON_TEXTURE = new Identifier(MODID, "textures/gui/wiki_button.png");
    public static final Style WIKI_LINK = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://andromeda-wiki.pages.dev/"));

    public static String DEBUG_SPLASH;
    public static ItemStack FRAME_STACK = ItemStack.EMPTY;
    private float tooltipFlow;

    @Override
    public void onInitializeClient() {
        ClientSideNetworking.register();
        registerEntityRenderers();
        registerBlockRenderers();

        inGameTooltips();

        ScreenEvents.BEFORE_INIT.register((client, screen1, scaledWidth, scaledHeight) -> {
            if (screen1 instanceof AbstractFurnaceScreen<?> abstractFurnaceScreen && Andromeda.CONFIG.guiParticles.furnaceScreenParticles) {
                ScreenEvents.afterTick(abstractFurnaceScreen).register(screen -> {
                    AbstractFurnaceScreen<?> furnaceScreen = (AbstractFurnaceScreen<?>) screen;
                    if (furnaceScreen.getScreenHandler().isBurning() && Utilities.RANDOM.nextInt(10) == 0) {
                        int x = ((HandledScreenAccessor) furnaceScreen).andromeda$getX();
                        int y = ((HandledScreenAccessor) furnaceScreen).andromeda$getY();


                        ScreenParticleHelper.addScreenParticle(ParticleTypes.FLAME,
                                MathStuff.nextDouble(Utilities.RANDOM, x + 56, x + 56 + 14),
                                y + 36 + 13, MathStuff.nextDouble(Utilities.RANDOM, -0.01, 0.01),
                                0.05);
                    }
                });
            }
        });

        if (Andromeda.CONFIG.usefulFletching)
            HandledScreens.register(Andromeda.FLETCHING_SCREEN_HANDLER, FletchingScreen::new);

        ParticleFactoryRegistry.getInstance().register(Andromeda.KNOCKOFF_TOTEM_PARTICLE, KnockoffTotemParticle.Factory::new);

        FabricLoader.getInstance().getModContainer(MODID).ifPresent(modContainer -> {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier(MODID, "dark"), modContainer, ResourcePackActivationType.NORMAL);
        });

        AndromedaAnalytics.handleUpload();
    }

    private void inGameTooltips() {
        HudRenderCallback.EVENT.register((context, delta) -> {
            if (Andromeda.CONFIG.itemFrameTooltips) {
                var client = MinecraftClient.getInstance();
                var cast = client.crosshairTarget;

                getCast(cast);

                if (!FRAME_STACK.isEmpty()) {
                    tooltipFlow = MathHelper.lerp(0.25f * client.getLastFrameDuration(), tooltipFlow, 1);
                    MatrixStack matrices = context.getMatrices();
                    matrices.push();
                    matrices.scale(1, 1, 1);
                    RenderSystem.setShaderColor(1, 1, 1, Math.min(tooltipFlow, 0.8f));
                    var list = Screen.getTooltipFromItem(MinecraftClient.getInstance(), FRAME_STACK);
                    list.add(AndromedaTexts.ITEM_IN_FRAME);
                    List<TooltipComponent> list1 = list.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());

                    FRAME_STACK.getTooltipData().ifPresent(datax -> list1.add(1, Utilities.supply(() -> {
                        TooltipComponent component = TooltipComponentCallback.EVENT.invoker().getComponent(datax);
                        if (component == null) component = TooltipComponent.of(datax);
                        return component;
                    })));

                    int j = 0;
                    for (TooltipComponent tooltipComponent : list1) {
                        j += tooltipComponent.getHeight();
                    }

                    DrawUtil.renderTooltipFromComponents(context, list1, ((client.getWindow().getScaledWidth() / 2f) - (tooltipFlow * 15)) + 15, ((client.getWindow().getScaledHeight() - j) / 2f) + 12);
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    matrices.pop();
                } else {
                    tooltipFlow = MathHelper.lerp(0.1f * client.getLastFrameDuration(), tooltipFlow, 0);
                }
            }
        });
    }

    private void getCast(HitResult cast) {
        if (cast != null) if (cast.getType() == HitResult.Type.ENTITY) {
            EntityHitResult hitResult = (EntityHitResult) cast;
            if (hitResult.getEntity() instanceof ItemFrameEntity itemFrameEntity) {
                FRAME_STACK = itemFrameEntity.getHeldItemStack();
                return;
            }
        }
        FRAME_STACK = ItemStack.EMPTY;
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
