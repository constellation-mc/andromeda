package dev.zenfyr.andromeda.modules.misc.unknown.mixin.wakeup;

import dev.zenfyr.andromeda.modules.misc.unknown.UnknownUtil;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class PlayerMixin {

  @Inject(at = @At("HEAD"), method = "stopSleepInBed(ZZ)V")
  private void andromeda$wakeUp(boolean forcefulWakeUp, boolean updateLevelList, CallbackInfo ci) {
    Player player = (Player) (Object) this;

    if (!player.level().isClientSide())
      if (player.level().getRandom().nextInt(100000) == 0) {
        Optional<BlockPos> optional = UnknownUtil.pickRandomSpot(
            player.level(), player.blockPosition(), 10, player.level().getRandom());
        if (optional.isPresent()) {
          BlockPos pos = optional.get();
          ArmorStand stand = new ArmorStand(player.level(), pos.getX(), pos.getY(), pos.getZ());
          ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

          stack.set(
              DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));

          stand.setItemSlot(EquipmentSlot.HEAD, stack);
          player.level().addFreshEntity(stand);
          ((ServerPlayer) player)
              .connection.send(new ClientboundSoundPacket(
                  BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.LIGHTNING_BOLT_THUNDER),
                  SoundSource.AMBIENT,
                  player.getX(),
                  player.getY(),
                  player.getZ(),
                  4,
                  1,
                  player.getRandom().nextLong()));
        }
      }
  }
}
