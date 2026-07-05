package dev.zenfyr.andromeda.modules.gui.gui_particles.mixin;

import com.google.common.collect.Lists;
import dev.zenfyr.andromeda.common.client.AndromedaClient;
import dev.zenfyr.andromeda.modules.gui.gui_particles.GuiParticles;
import dev.zenfyr.pulsar.api.client.particles.ItemStackParticle;
import dev.zenfyr.pulsar.api.client.particles.ScreenParticles;
import dev.zenfyr.pulsar.api.client.particles.VanillaParticles;
import dev.zenfyr.pulsar.api.util.MathUtil;
import dev.zenfyr.pulsar.api.util.Utilities;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameModeSwitcherScreen.class)
abstract class GameModeSelectionScreenMixin extends Screen {

  @Shadow
  protected abstract void init();

  @Unique private static final List<ItemStack> ANDROMEDA$ADVENTURE = Lists.newArrayList(
      Items.COMPASS.getDefaultInstance(),
      Items.MAP.getDefaultInstance(),
      Items.FILLED_MAP.getDefaultInstance());

  @Unique private static final List<ItemStack> ANDROMEDA$SURVIVAL = Lists.newArrayList(
      Items.IRON_SWORD.getDefaultInstance(),
      Items.APPLE.getDefaultInstance(),
      Items.DIAMOND.getDefaultInstance(),
      Items.LEATHER_BOOTS.getDefaultInstance(),
      Items.ROTTEN_FLESH.getDefaultInstance(),
      Items.ENDER_PEARL.getDefaultInstance());

  @Unique private static final List<ItemStack> ANDROMEDA$SPECTATOR =
      Lists.newArrayList(Items.ENDER_EYE.getDefaultInstance());

  @Unique private static final Map<GameModeSwitcherScreen.GameModeIcon, Supplier<ItemStack>>
      ANDROMEDA$GAME_MODE_STACKS =
          Utilities.supply(new EnumMap<>(GameModeSwitcherScreen.GameModeIcon.class), map -> {
            map.put(
                GameModeSwitcherScreen.GameModeIcon.CREATIVE,
                () -> BuiltInRegistries.ITEM
                    .getRandom(RandomSource.create())
                    .orElseThrow()
                    .value()
                    .getDefaultInstance());
            map.put(
                GameModeSwitcherScreen.GameModeIcon.ADVENTURE,
                () -> Utilities.pickAtRandom(ANDROMEDA$ADVENTURE));
            map.put(
                GameModeSwitcherScreen.GameModeIcon.SURVIVAL,
                () -> Utilities.pickAtRandom(ANDROMEDA$SURVIVAL));
            map.put(
                GameModeSwitcherScreen.GameModeIcon.SPECTATOR,
                () -> Utilities.pickAtRandom(ANDROMEDA$SPECTATOR));
          });

  protected GameModeSelectionScreenMixin(Component title) {
    super(title);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V",
              shift = At.Shift.BEFORE),
      method =
          "switchToHoveredGameMode(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/debug/GameModeSwitcherScreen$GameModeIcon;)V")
  private static void andromeda$gmSwitchParticles(
      Minecraft minecraft, GameModeSwitcherScreen.GameModeIcon toGameMode, CallbackInfo ci) {
    if (!AndromedaClient.CLIENT.get(GuiParticles.CONFIG).gameModeSwitcherParticles) return;

    if (minecraft.screen instanceof GameModeSwitcherScreen gameModeSelectionScreen) {
      List<GameModeSwitcherScreen.GameModeSlot> buttonWidgets =
          new ArrayList<>(gameModeSelectionScreen.slots);
      buttonWidgets.removeIf(buttonWidget -> buttonWidget.icon != toGameMode);
      Optional<GameModeSwitcherScreen.GameModeSlot> optional =
          buttonWidgets.stream().findFirst();

      if (optional.isPresent()) {
        GameModeSwitcherScreen.GameModeSlot widget = optional.get();
        double x = widget.getX() + widget.getWidth() / 2d;
        double y = widget.getY() + widget.getHeight() / 2d;

        var particles = ScreenParticles.get(minecraft);
        if (ANDROMEDA$GAME_MODE_STACKS.containsKey(toGameMode)) {
          for (int i = 0; i < 5; i++) {
            particles.addParticle(new ItemStackParticle(
                x,
                y,
                MathUtil.nextDouble(-2, 2),
                MathUtil.nextDouble(-2, 2),
                ANDROMEDA$GAME_MODE_STACKS.get(toGameMode).get()));
          }
        } else {
          particles.addParticles(
              VanillaParticles.create(ParticleTypes.END_ROD, x, y, 0.5, 0.5, 0.07, 10));
        }
      }
    }
  }
}
