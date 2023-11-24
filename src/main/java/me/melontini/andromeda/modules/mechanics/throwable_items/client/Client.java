package me.melontini.andromeda.modules.mechanics.throwable_items.client;

import me.melontini.andromeda.modules.mechanics.throwable_items.Content;
import me.melontini.andromeda.util.AndromedaPackets;
import me.melontini.dark_matter.api.base.util.ColorUtil;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.particle.Particle;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;

import static me.melontini.dark_matter.api.base.util.MathStuff.threadRandom;

public class Client {

    public static void init() {
        Content.FLYING_ITEM.ifPresent(e -> EntityRendererRegistry.register(e, FlyingItemEntityRenderer::new));

        ClientPlayNetworking.registerGlobalReceiver(AndromedaPackets.FLYING_STACK_LANDED, (client, handler, buf, responseSender) -> {
            double x = buf.readDouble(), y = buf.readDouble(), z = buf.readDouble();
            boolean spawnItem = buf.readBoolean();
            ItemStack stack = buf.readItemStack();
            boolean spawnColor = buf.readBoolean();

            int color = 0;
            if (spawnColor) color = buf.readVarInt();

            float r = ColorUtil.getRedF(color), g = ColorUtil.getGreenF(color), b = ColorUtil.getBlueF(color);
            client.execute(() -> {
                ParticlesMode particlesMode = MinecraftClient.getInstance().options.getParticles().getValue();
                if (particlesMode == ParticlesMode.MINIMAL) return;

                if (spawnItem) for (int i = 0; i < (particlesMode != ParticlesMode.DECREASED ? 8 : 4); ++i) {
                    MinecraftClient.getInstance().particleManager.addParticle(
                            new ItemStackParticleEffect(ParticleTypes.ITEM, stack),
                            x, y, z,
                            threadRandom().nextGaussian() * 0.15,
                            threadRandom().nextDouble() * 0.2,
                            threadRandom().nextGaussian() * 0.15
                    );
                }

                if (spawnColor) for (int i = 0; i < (particlesMode != ParticlesMode.DECREASED ? 15 : 7); i++) {
                    Particle particle = MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.EFFECT, x, y, z,
                            threadRandom().nextGaussian() * 0.15, 0.5, threadRandom().nextGaussian() * 0.15);
                    if (particle != null) particle.setColor(r, g, b);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(AndromedaPackets.COLORED_FLYING_STACK_LANDED, (client, handler, buf, responseSender) -> {
            ItemStack dye = buf.readItemStack();
            client.execute(() -> {
                int a = client.getWindow().getScaledWidth();
                ScreenParticleHelper.addParticle(new DyeParticle(MathStuff.nextDouble(a / 2d - (a / 3d), a / 2d + a / 3d), client.getWindow().getScaledHeight() / 2d, 0, 0, dye));
            });
        });
    }
}
