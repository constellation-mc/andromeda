package me.melontini.andromeda.mixin.entities.furnace_minecart;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FurnaceMinecartEntity.class)
@MixinRelatedConfigOption("betterFurnaceMinecart")
public class FurnaceMinecartMixin {
    @Shadow
    public int fuel;

    @Inject(at = @At("HEAD"), method = "interact", cancellable = true)
    public void andromeda$interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!Andromeda.CONFIG.betterFurnaceMinecart) return;
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();

        FurnaceMinecartEntity furnaceMinecart = (FurnaceMinecartEntity) (Object) this;
        if (FuelRegistry.INSTANCE.get(item) != null) {
            int itemFuel = FuelRegistry.INSTANCE.get(item);
            if ((this.fuel + (itemFuel * 2.25)) <= Andromeda.CONFIG.maxFurnaceMinecartFuel) {
                if (!player.getAbilities().creativeMode) {
                    if (stack.getItem().getRecipeRemainder() != null)
                        player.getInventory().offerOrDrop(stack.getItem().getRecipeRemainder(stack));
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

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;putShort(Ljava/lang/String;S)V"), method = "writeCustomDataToNbt")
    private void andromeda$fuelIntToNbt(NbtCompound nbt, String key, /* short */ short value, Operation<Void> operation) {
        if (Andromeda.CONFIG.betterFurnaceMinecart) nbt.putInt(key, this.fuel);
        else operation.call(nbt, key, value);
    }

    @Inject(at = @At(value = "TAIL"), method = "readCustomDataFromNbt")
    public void andromeda$fuelIntFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (Andromeda.CONFIG.betterFurnaceMinecart) this.fuel = nbt.getInt("Fuel");
    }
}
