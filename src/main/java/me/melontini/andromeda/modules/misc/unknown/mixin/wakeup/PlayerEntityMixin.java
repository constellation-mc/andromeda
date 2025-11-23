package me.melontini.andromeda.modules.misc.unknown.mixin.wakeup;

import java.util.Optional;
import me.melontini.andromeda.modules.misc.unknown.UnknownUtil;
import me.melontini.dark_matter.api.data.nbt.NbtBuilder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class PlayerEntityMixin {

  @Shadow
  public abstract void playNotifySound(
          SoundEvent event, SoundSource category, float volume, float pitch);

  @Inject(at = @At("HEAD"), method = "stopSleepInBed(ZZ)V")
  private void andromeda$wakeUp(
      boolean skipSleepTimer, boolean updateSleepingPlayers, CallbackInfo ci) {
    Player player = (Player) (Object) this;

    if (!player.level.isClientSide)
      if (player.level.getRandom().nextInt(100000) == 0) {
        Optional<BlockPos> optional = UnknownUtil.pickRandomSpot(
            player.level, player.blockPosition(), 10, player.level.getRandom());
        if (optional.isPresent()) {
          BlockPos pos = optional.get();
          ArmorStand stand =
              new ArmorStand(player.level, pos.getX(), pos.getY(), pos.getZ());
          ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

          stack.setTag(NbtBuilder.create()
              .putString("SkullOwner", player.getDisplayName().getString())
              .build());

          stand.setItemSlot(EquipmentSlot.HEAD, stack);
          player.level.addFreshEntity(stand);
          playNotifySound(SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.AMBIENT, 4, 1);
        }
      }
  }
}
