package me.melontini.andromeda.modules.items.lava_disintegrator.mixin;

import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Item.class)
abstract class ItemMixin {

    @Inject(at = @At("HEAD"), method = "onClicked", cancellable = true)
    private void andromeda$onLavaClick(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
        if (clickType == ClickType.RIGHT && stack.isOf(Items.LAVA_BUCKET)) {
            if (otherStack.getItem().isFireproof() || EnchantmentHelper.getLevel(Enchantments.FIRE_PROTECTION, otherStack) > 0) return;

            cursorStackReference.set(ItemStack.EMPTY);
            if (player.world.isClient) spawnLavaParticles((int) Math.max(2, Math.sqrt(otherStack.getCount())));
            cir.setReturnValue(true);
        }
    }

    @Unique @Environment(EnvType.CLIENT)
    private static void spawnLavaParticles(int count) {
        var client = MinecraftClient.getInstance();
        int x = (int) (client.mouse.getX() * (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth());
        int y = (int) (client.mouse.getY() * (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight());
        for (int i = 0; i < count; i++) {
            ScreenParticleHelper.addParticle(ParticleTypes.LAVA, x, y, 0.0, 0.0);
        }
        Objects.requireNonNull(client.player).playSound(SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.AMBIENT, 0.8f, 0.8F + MathUtil.threadRandom().nextFloat() * 0.4F);
    }
}
