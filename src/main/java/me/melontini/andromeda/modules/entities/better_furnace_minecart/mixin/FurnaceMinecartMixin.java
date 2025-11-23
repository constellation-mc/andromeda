package me.melontini.andromeda.modules.entities.better_furnace_minecart.mixin;

import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.modules.entities.better_furnace_minecart.BetterFurnaceMinecart;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartFurnace.class)
abstract class FurnaceMinecartMixin {

  @Shadow
  public int fuel;

  @Inject(at = @At("HEAD"), method = "interact", cancellable = true)
  public void andromeda$interact(
      Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
    ItemStack stack = player.getItemInHand(hand);
    Item item = stack.getItem();

    MinecartFurnace furnaceMinecart = (MinecartFurnace) (Object) this;
    if (FuelRegistry.INSTANCE.get(item) != null) {
      int itemFuel = FuelRegistry.INSTANCE.get(item);
      if ((this.fuel + (itemFuel * 2.25))
          <= Andromeda.MAIN.get(BetterFurnaceMinecart.CONFIG).maxFuel) {
        if (!player.getAbilities().instabuild) {
          ItemStack reminder = stack.getRecipeRemainder();
          if (!reminder.isEmpty())
            player.getInventory().placeItemBackInInventory(stack.getRecipeRemainder());
          stack.shrink(1);
        }

        this.fuel += (int) (itemFuel * 2.25);
      }
    }

    if (this.fuel > 0) {
      furnaceMinecart.xPush = furnaceMinecart.getX() - player.getX();
      furnaceMinecart.zPush = furnaceMinecart.getZ() - player.getZ();
    }

    cir.setReturnValue(InteractionResult.sidedSuccess(furnaceMinecart.level.isClientSide));
  }

  @Redirect(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/nbt/CompoundTag;putShort(Ljava/lang/String;S)V"),
      method = "addAdditionalSaveData")
  private void andromeda$fuelIntToNbt(CompoundTag nbt, String key, short value /* short */) {
    nbt.putInt(key, this.fuel);
  }

  @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
  public void andromeda$fuelIntFromNbt(CompoundTag nbt, CallbackInfo ci) {
    this.fuel = nbt.getInt("Fuel");
  }
}
