package me.melontini.andromeda.modules.gui.gui_particles.mixin;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.gui.gui_particles.GuiParticles;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.fabricmc.api.EnvType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 0), method = "onTakeOutput")
    private void andromeda$particles(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!ModuleManager.quick(GuiParticles.class).config().anvilScreenParticles) return;

        Support.run(EnvType.CLIENT, () -> () -> {
            if (MinecraftClient.getInstance().isOnThread() && MinecraftClient.getInstance().currentScreen instanceof AnvilScreen anvilScreen) {
                BlockState state = Blocks.ANVIL.getDefaultState();
                var slot = this.slots.get(2);
                boolean enchant = this.slots.get(1).getStack().isOf(Items.ENCHANTED_BOOK);
                ScreenParticleHelper.addScreenParticles(
                        !enchant ? new BlockStateParticleEffect(ParticleTypes.BLOCK, state) : ParticleTypes.END_ROD,
                        anvilScreen.x + slot.x + 8, anvilScreen.y + slot.y + 8,
                        0.5, 0.5, !enchant ? 0.5 : 0.07, 5);
            }
        });
    }
}
