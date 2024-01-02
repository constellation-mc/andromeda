package me.melontini.andromeda.modules.gui.gui_particles.mixin;

import com.google.common.collect.Lists;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.modules.gui.gui_particles.GuiParticles;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import me.melontini.dark_matter.api.glitter.particles.ItemStackParticle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Supplier;

@Mixin(GameModeSelectionScreen.class)
abstract class GameModeSelectionScreenMixin extends Screen {
    @Unique
    private static final GuiParticles am$guip = ModuleManager.quick(GuiParticles.class);

    @Shadow protected abstract void init();

    @Unique
    private static final List<ItemStack> ANDROMEDA$ADVENTURE = Lists.newArrayList(Items.COMPASS.getDefaultStack(), Items.MAP.getDefaultStack(), Items.FILLED_MAP.getDefaultStack());
    @Unique
    private static final List<ItemStack> ANDROMEDA$SURVIVAL = Lists.newArrayList(Items.IRON_SWORD.getDefaultStack(), Items.APPLE.getDefaultStack(), Items.DIAMOND.getDefaultStack(), Items.LEATHER_BOOTS.getDefaultStack(), Items.ROTTEN_FLESH.getDefaultStack(), Items.ENDER_PEARL.getDefaultStack());
    @Unique
    private static final List<ItemStack> ANDROMEDA$SPECTATOR = Lists.newArrayList(Items.ENDER_EYE.getDefaultStack());

    @Unique
    private static final Map<GameModeSelectionScreen.GameModeSelection, Supplier<ItemStack>> ANDROMEDA$GAME_MODE_STACKS = Utilities.consume(new EnumMap<>(GameModeSelectionScreen.GameModeSelection.class), map -> {
        map.put(GameModeSelectionScreen.GameModeSelection.CREATIVE, () -> CommonRegistries.items().getRandom(Random.create()).orElseThrow().value().getDefaultStack());
        map.put(GameModeSelectionScreen.GameModeSelection.ADVENTURE, () -> Utilities.pickAtRandom(ANDROMEDA$ADVENTURE));
        map.put(GameModeSelectionScreen.GameModeSelection.SURVIVAL, () -> Utilities.pickAtRandom(ANDROMEDA$SURVIVAL));
        map.put(GameModeSelectionScreen.GameModeSelection.SPECTATOR, () -> Utilities.pickAtRandom(ANDROMEDA$SPECTATOR));
    });

    protected GameModeSelectionScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendCommand(Ljava/lang/String;)Z", shift = At.Shift.BEFORE), method = "apply(Lnet/minecraft/client/MinecraftClient;Ljava/util/Optional;)V")
    private static void andromeda$gmSwitchParticles(MinecraftClient client, Optional<GameModeSelectionScreen.GameModeSelection> gameMode, CallbackInfo ci) {
        if (gameMode.isEmpty() || !am$guip.config().gameModeSwitcherParticles) return;

        if (client.currentScreen instanceof GameModeSelectionScreen gameModeSelectionScreen) {
            List<GameModeSelectionScreen.ButtonWidget> buttonWidgets = new ArrayList<>(gameModeSelectionScreen.gameModeButtons);
            buttonWidgets.removeIf(buttonWidget -> buttonWidget.gameMode != gameMode.get());
            Optional<GameModeSelectionScreen.ButtonWidget> optional = buttonWidgets.stream().findFirst();

            if (optional.isPresent()) {
                GameModeSelectionScreen.ButtonWidget widget = optional.get();
                double x = widget.x + widget.getWidth() / 2d;
                double y = widget.y + widget.getHeight() / 2d;

                if (ANDROMEDA$GAME_MODE_STACKS.containsKey(gameMode.get())) {
                    ScreenParticleHelper.addParticles(() -> new ItemStackParticle(
                            x, y,
                            MathStuff.nextDouble(-2, 2), MathStuff.nextDouble(-2, 2),
                            ANDROMEDA$GAME_MODE_STACKS.get(gameMode.get()).get()), 5);
                } else {
                    ScreenParticleHelper.addParticles(ParticleTypes.END_ROD, x, y, 0.5, 0.5, 0.07, 10);
                }
            }
        }
    }
}
