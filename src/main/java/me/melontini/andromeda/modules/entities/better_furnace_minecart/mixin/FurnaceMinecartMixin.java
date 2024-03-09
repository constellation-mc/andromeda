package me.melontini.andromeda.modules.entities.better_furnace_minecart.mixin;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.better_furnace_minecart.BetterFurnaceMinecart;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FurnaceMinecartEntity.class)
abstract class FurnaceMinecartMixin {

    @Shadow public int fuel;

    @Inject(at = @At("HEAD"), method = "interact", cancellable = true)
    public void andromeda$interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();

        FurnaceMinecartEntity furnaceMinecart = (FurnaceMinecartEntity) (Object) this;
        if (FuelRegistry.INSTANCE.get(item) != null) {
            int itemFuel = FuelRegistry.INSTANCE.get(item);
            if ((this.fuel + (itemFuel * 2.25)) <= ModuleManager.quick(BetterFurnaceMinecart.class).config().maxFuel) {
                if (!player.getAbilities().creativeMode) {
                    ItemStack reminder = stack.getRecipeRemainder();
                    if (!reminder.isEmpty())
                        player.getInventory().offerOrDrop(stack.getRecipeRemainder());
                    stack.decrement(1);
                }

                this.fuel += (int) (itemFuel * 2.25);
            }
        }

        if (this.fuel > 0) {
            furnaceMinecart.pushX = furnaceMinecart.getX() - player.getX();
            furnaceMinecart.pushZ = furnaceMinecart.getZ() - player.getZ();
        }

        cir.setReturnValue(ActionResult.success(furnaceMinecart.world.isClient));
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;putShort(Ljava/lang/String;S)V"), method = "writeCustomDataToNbt")
    private void andromeda$fuelIntToNbt(NbtCompound nbt, String key, short value  /* short */) {
        nbt.putInt(key, this.fuel);
    }

    @Inject(at = @At(value = "TAIL"), method = "readCustomDataFromNbt")
    public void andromeda$fuelIntFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.fuel = nbt.getInt("Fuel");
    }
}
