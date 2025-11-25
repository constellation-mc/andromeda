package dev.zenfyr.andromeda.modules.mechanics.throwable_items.client;

import static me.melontini.dark_matter.api.base.util.MathUtil.threadRandom;

import java.util.HashSet;
import java.util.Set;
import dev.zenfyr.andromeda.common.AndromedaClient;
import dev.zenfyr.andromeda.modules.mechanics.throwable_items.Main;
import dev.zenfyr.andromeda.modules.mechanics.throwable_items.ThrowableItems;
import me.melontini.dark_matter.api.base.util.ColorUtil;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class Client {

  private static final Set<Item> showTooltip = new HashSet<>();

  public static void init() {
    if (Main.FLYING_ITEM.isPresent()) {
      EntityRendererRegistry.register(Main.FLYING_ITEM.orThrow(), FlyingItemEntityRenderer::new);
    }

    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> showTooltip.clear());
    ClientPlayNetworking.registerGlobalReceiver(
        Main.ITEMS_WITH_BEHAVIORS, (client, handler, buf, responseSender) -> {
          Set<ResourceLocation> ids = new HashSet<>();
          int length = buf.readVarInt();
          for (int i = 0; i < length; i++) ids.add(buf.readResourceLocation());
          client.execute(() -> {
            showTooltip.clear();
            for (ResourceLocation id : ids) showTooltip.add(BuiltInRegistries.ITEM.get(id));
          });
        });

    var config = AndromedaClient.CLIENT.get(ThrowableItems.CLIENT_CONFIG);
    ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
      if (config.tooltip && showTooltip.contains(stack.getItem())) {
        lines.add(TextUtil.translatable("tooltip.andromeda.throwable_item")
            .withStyle(ChatFormatting.GRAY));
      }
    });

    ClientPlayNetworking.registerGlobalReceiver(
        Main.FLYING_STACK_LANDED, (client, handler, buf, responseSender) -> {
          double x = buf.readDouble(), y = buf.readDouble(), z = buf.readDouble();
          boolean spawnItem = buf.readBoolean();
          ItemStack stack = buf.readItem();
          boolean spawnColor = buf.readBoolean();

          int color = 0;
          if (spawnColor) color = buf.readVarInt();

          float r = ColorUtil.getRedF(color),
              g = ColorUtil.getGreenF(color),
              b = ColorUtil.getBlueF(color);
          client.execute(() -> {
            ParticleStatus particlesMode =
                Minecraft.getInstance().options.particles().get();
            if (particlesMode == ParticleStatus.MINIMAL) return;

            if (spawnItem)
              for (int i = 0; i < (particlesMode != ParticleStatus.DECREASED ? 8 : 4); ++i) {
                Minecraft.getInstance()
                    .particleEngine
                    .createParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, stack),
                        x,
                        y,
                        z,
                        threadRandom().nextGaussian() * 0.15,
                        threadRandom().nextDouble() * 0.2,
                        threadRandom().nextGaussian() * 0.15);
              }

            if (spawnColor)
              for (int i = 0; i < (particlesMode != ParticleStatus.DECREASED ? 15 : 7); i++) {
                Particle particle = Minecraft.getInstance()
                    .particleEngine
                    .createParticle(
                        ParticleTypes.EFFECT,
                        x,
                        y,
                        z,
                        threadRandom().nextGaussian() * 0.15,
                        0.5,
                        threadRandom().nextGaussian() * 0.15);
                if (particle != null) particle.setColor(r, g, b);
              }
          });
        });

    ClientPlayNetworking.registerGlobalReceiver(
        Main.COLORED_FLYING_STACK_LANDED, (client, handler, buf, responseSender) -> {
          ItemStack dye = buf.readItem();
          client.execute(() -> {
            int a = client.getWindow().getGuiScaledWidth();
            ScreenParticleHelper.addParticle(new DyeParticle(
                MathUtil.nextDouble(a / 2d - (a / 3d), a / 2d + a / 3d),
                client.getWindow().getGuiScaledHeight() / 2d,
                0,
                0,
                dye));
          });
        });
  }
}
