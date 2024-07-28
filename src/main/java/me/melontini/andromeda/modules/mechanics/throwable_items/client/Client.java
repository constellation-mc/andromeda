package me.melontini.andromeda.modules.mechanics.throwable_items.client;

import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.andromeda.modules.mechanics.throwable_items.ThrowableItems;
import me.melontini.andromeda.modules.mechanics.throwable_items.packets.ColoredStackLandedPayload;
import me.melontini.andromeda.modules.mechanics.throwable_items.packets.FlyingStackLandedPayload;
import me.melontini.andromeda.modules.mechanics.throwable_items.packets.ItemBehaviorsPayload;
import me.melontini.dark_matter.api.base.util.ColorUtil;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.particle.Particle;
import net.minecraft.item.Item;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;

import static me.melontini.dark_matter.api.base.util.MathUtil.threadRandom;

public final class Client {

    private static final Set<Item> showTooltip = new HashSet<>();

    public static void init(ThrowableItems.ClientConfig config) {
        Main.FLYING_ITEM.ifPresent(e -> EntityRendererRegistry.register(e, FlyingItemEntityRenderer::new));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> showTooltip.clear());
        ClientPlayNetworking.registerGlobalReceiver(ItemBehaviorsPayload.ID, (payload, context) -> context.client().execute(() -> {
            showTooltip.clear();
            showTooltip.addAll(payload.items());
        }));

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (config.tooltip && showTooltip.contains(stack.getItem())) {
                lines.add(TextUtil.translatable("tooltip.andromeda.throwable_item").formatted(Formatting.GRAY));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(FlyingStackLandedPayload.ID, (payload, context) -> context.client().execute(() -> {
            ParticlesMode particlesMode = MinecraftClient.getInstance().options.getParticles().getValue();
            if (particlesMode == ParticlesMode.MINIMAL) return;
            float x = payload.pos().x(), y = payload.pos().y(), z = payload.pos().z();

            payload.stack().ifPresent(stack -> {
                for (int i = 0; i < (particlesMode != ParticlesMode.DECREASED ? 8 : 4); ++i) {
                    MinecraftClient.getInstance().particleManager.addParticle(
                            new ItemStackParticleEffect(ParticleTypes.ITEM, stack),
                            x, y, z,
                            threadRandom().nextGaussian() * 0.15,
                            threadRandom().nextDouble() * 0.2,
                            threadRandom().nextGaussian() * 0.15
                    );
                }
            });

            payload.color().ifPresent(color -> {
                float r = ColorUtil.getRedF(color), g = ColorUtil.getGreenF(color), b = ColorUtil.getBlueF(color);

                for (int i = 0; i < (particlesMode != ParticlesMode.DECREASED ? 15 : 7); i++) {
                    Particle particle = MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.EFFECT, x, y, z,
                            threadRandom().nextGaussian() * 0.15, 0.5, threadRandom().nextGaussian() * 0.15);
                    if (particle != null) particle.setColor(r, g, b);
                }
            });
        }));

        ClientPlayNetworking.registerGlobalReceiver(ColoredStackLandedPayload.ID, (payload, context) -> context.client().execute(() -> {
            int a = context.client().getWindow().getScaledWidth();
            ScreenParticleHelper.addParticle(new DyeParticle(MathUtil.nextDouble(a / 2d - (a / 3d), a / 2d + a / 3d), context.client().getWindow().getScaledHeight() / 2d, 0, 0, payload.dye()));
        }));
    }
}
