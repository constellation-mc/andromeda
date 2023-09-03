package me.melontini.andromeda.mixin.gui.gui_particles;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
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
@MixinRelatedConfigOption("guiParticles.anvilScreenParticles")
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 0), method = "onTakeOutput")
    private void andromeda$particles(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!Config.get().guiParticles.anvilScreenParticles) return;

        try {
            if (MinecraftClient.getInstance().isOnThread() && MinecraftClient.getInstance().currentScreen instanceof AnvilScreen anvilScreen) {
                BlockState state = Blocks.ANVIL.getDefaultState();
                var slot = this.slots.get(2);
                boolean enchant = this.slots.get(1).getStack().isOf(Items.ENCHANTED_BOOK);
                ScreenParticleHelper.addScreenParticles(
                        !enchant ? new BlockStateParticleEffect(ParticleTypes.BLOCK, state) : ParticleTypes.END_ROD,
                        anvilScreen.x + slot.x + 8, anvilScreen.y + slot.y + 8,
                        0.5, 0.5, !enchant ? 0.5 : 0.07, 5);
            }
        } catch (Exception e) {
            //client-server handling 101
        }
    }
}
