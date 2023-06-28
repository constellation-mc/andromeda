package me.melontini.andromeda.mixin.gui.gui_particles;

import com.google.common.collect.Lists;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.glitter.client.particles.ItemStackParticle;
import me.melontini.dark_matter.glitter.client.util.ScreenParticleHelper;
import me.melontini.dark_matter.util.MathStuff;
import me.melontini.dark_matter.util.Utilities;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(GameModeSelectionScreen.class)
@MixinRelatedConfigOption("guiParticles.gameModeSwitcherParticles")
public abstract class GameModeSelectionScreenMixin extends Screen {
    private static final Map<GameModeSelectionScreen.GameModeSelection, List<ItemStack>> GAME_MODE_STACKS = Utilities.consume(new HashMap<>(), map -> {
        map.put(GameModeSelectionScreen.GameModeSelection.CREATIVE, Registry.ITEM.stream().map(Item::getDefaultStack).toList());
        map.put(GameModeSelectionScreen.GameModeSelection.ADVENTURE, Lists.newArrayList(Items.COMPASS.getDefaultStack(), Items.MAP.getDefaultStack(), Items.FILLED_MAP.getDefaultStack()));
        map.put(GameModeSelectionScreen.GameModeSelection.SURVIVAL, Lists.newArrayList(Items.IRON_SWORD.getDefaultStack(), Items.APPLE.getDefaultStack(), Items.DIAMOND.getDefaultStack(), Items.LEATHER_BOOTS.getDefaultStack(), Items.ROTTEN_FLESH.getDefaultStack(), Items.ENDER_PEARL.getDefaultStack()));
        map.put(GameModeSelectionScreen.GameModeSelection.SPECTATOR, Lists.newArrayList(Items.ENDER_EYE.getDefaultStack()));
    });

    @Shadow
    protected abstract void init();

    protected GameModeSelectionScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendCommand(Ljava/lang/String;)Z", shift = At.Shift.BEFORE), method = "apply(Lnet/minecraft/client/MinecraftClient;Ljava/util/Optional;)V")
    private static void andromeda$gmSwitchParticles(MinecraftClient client, Optional<GameModeSelectionScreen.GameModeSelection> gameMode, CallbackInfo ci) {
        if (gameMode.isEmpty() || !Andromeda.CONFIG.guiParticles.gameModeSwitcherParticles) return;

        if (client.currentScreen instanceof GameModeSelectionScreen gameModeSelectionScreen) {
            List<GameModeSelectionScreen.ButtonWidget> buttonWidgets = new ArrayList<>(gameModeSelectionScreen.gameModeButtons);
            buttonWidgets.removeIf(buttonWidget -> buttonWidget.gameMode != gameMode.get());
            Optional<GameModeSelectionScreen.ButtonWidget> optional = buttonWidgets.stream().findFirst();

            if (optional.isPresent()) {
                GameModeSelectionScreen.ButtonWidget widget = optional.get();
                double x = widget.x + widget.getWidth() / 2d;
                double y = widget.y + widget.getHeight() / 2d;

                if (GAME_MODE_STACKS.containsKey(gameMode.get())) {
                    var list = GAME_MODE_STACKS.get(gameMode.get());
                    if (list.isEmpty()) return;

                    ScreenParticleHelper.addParticles(() -> new ItemStackParticle(
                            x, y,
                            MathStuff.nextDouble(Utilities.RANDOM, -2, 2),
                            MathStuff.nextDouble(Utilities.RANDOM, -2, 2),
                            Utilities.pickAtRandom(list)), 5);
                } else {
                    ScreenParticleHelper.addParticles(ParticleTypes.END_ROD, x, y, 0.5, 0.5, 0.07, 10);
                }
            }
        }
    }
}
