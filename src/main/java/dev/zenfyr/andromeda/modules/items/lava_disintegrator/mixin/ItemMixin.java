package dev.zenfyr.andromeda.modules.items.lava_disintegrator.mixin;

import dev.zenfyr.pulsar.api.client.particles.ScreenParticleHelper;
import dev.zenfyr.pulsar.api.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
abstract class ItemMixin {

  @Inject(at = @At("HEAD"), method = "overrideOtherStackedOnMe", cancellable = true)
  private void andromeda$onLavaClick(
      ItemStack stack,
      ItemStack otherStack,
      Slot slot,
      ClickAction clickType,
      Player player,
      SlotAccess cursorStackReference,
      CallbackInfoReturnable<Boolean> cir) {
    if (clickType == ClickAction.SECONDARY && stack.is(Items.LAVA_BUCKET)) {
      var damageResistant = otherStack.get(DataComponents.DAMAGE_RESISTANT);
      if (damageResistant != null
          && damageResistant.isResistantTo(player.level.damageSources().inFire())) {
        return;
      }

      if (EnchantmentHelper.getItemEnchantmentLevel(
              player.level.registryAccess().getOrThrow(Enchantments.FIRE_PROTECTION), otherStack)
          > 0) return;

      cursorStackReference.set(ItemStack.EMPTY);
      if (player.level.isClientSide())
        spawnLavaParticles((int) Math.max(2, Math.sqrt(otherStack.getCount())));
      cir.setReturnValue(true);
    }
  }

  @Unique @Environment(EnvType.CLIENT)
  private static void spawnLavaParticles(int count) {
    var client = Minecraft.getInstance();
    int x = (int) (client.mouseHandler.xpos()
        * (double) client.getWindow().getGuiScaledWidth()
        / (double) client.getWindow().getScreenWidth());
    int y = (int) (client.mouseHandler.ypos()
        * (double) client.getWindow().getGuiScaledHeight()
        / (double) client.getWindow().getScreenHeight());
    for (int i = 0; i < count; i++) {
      ScreenParticleHelper.addParticle(ParticleTypes.LAVA, x, y, 0.0, 0.0);
    }

    client.level.playSound(
        client.player,
        client.player,
        SoundEvents.LAVA_EXTINGUISH,
        SoundSource.AMBIENT,
        0.8f,
        0.8F + MathUtil.threadRandom().nextFloat() * 0.4F);
  }
}
