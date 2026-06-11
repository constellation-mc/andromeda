package dev.zenfyr.andromeda.modules.entities.furnace_minecart_tweaks.mixin;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.entities.furnace_minecart_tweaks.FurnaceMinecartTweaks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
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
      Player player,
      InteractionHand hand,
      Vec3 location,
      CallbackInfoReturnable<InteractionResult> cir) {
    ItemStack stack = player.getItemInHand(hand);

    MinecartFurnace furnaceMinecart = (MinecartFurnace) (Object) this;
    if (furnaceMinecart.level.fuelValues().isFuel(stack)) {
      int itemFuel = furnaceMinecart.level.fuelValues().burnDuration(stack);
      if ((this.fuel + (itemFuel * 2.25))
          <= Andromeda.MAIN.get(FurnaceMinecartTweaks.CONFIG).maxFuel) {
        if (!player.getAbilities().instabuild) {
          ItemStackTemplate reminder = stack.getCraftingRemainder();
          if (reminder != null) player.getInventory().placeItemBackInInventory(reminder.create());
          stack.shrink(1);
        }

        this.fuel += (int) (itemFuel * 2.25);
      }
    }

    if (this.fuel > 0) {
      furnaceMinecart.push =
          furnaceMinecart.position().subtract(player.position()).horizontal();
    }

    cir.setReturnValue(InteractionResult.SUCCESS);
  }

  @Redirect(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/storage/ValueOutput;putShort(Ljava/lang/String;S)V"),
      method = "addAdditionalSaveData")
  private void andromeda$fuelIntToNbt(ValueOutput instance, String key, short i /* short */) {
    instance.putInt(key, this.fuel);
  }

  @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
  public void andromeda$fuelIntFromNbt(ValueInput valueInput, CallbackInfo ci) {
    this.fuel = valueInput.getIntOr("Fuel", 0);
  }
}
