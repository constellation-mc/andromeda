package me.melontini.andromeda.mixin.items.cart_copy;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.minecraft.data.NbtUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net/minecraft/item/MinecartItem$1")
@Feature("minecartBlockPicking")
class ItemDispenserBehaviorMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;create(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/vehicle/AbstractMinecartEntity$Type;)Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, method = "dispenseSilently", cancellable = true)
    public void andromeda$dispenseSilently(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir, Direction direction, World world, double d, double e, double f, BlockPos blockPos, BlockState blockState, RailShape railShape, double g) {
        if (stack.getItem() == Items.CHEST_MINECART) {
            AbstractMinecartEntity abstractMinecartEntity = AbstractMinecartEntity.create(world, d, e + g, f, AbstractMinecartEntity.Type.CHEST);
            ChestMinecartEntity chestMinecart = (ChestMinecartEntity) abstractMinecartEntity;

            NbtUtil.readInventoryFromNbt(stack.getNbt(), chestMinecart);
            if (stack.hasCustomName()) chestMinecart.setCustomName(stack.getName());

            world.spawnEntity(chestMinecart);
            stack.decrement(1);
            cir.setReturnValue(stack);
        } else if (stack.getItem() == Items.HOPPER_MINECART) {
            AbstractMinecartEntity abstractMinecartEntity = AbstractMinecartEntity.create(world, d, e + g, f, AbstractMinecartEntity.Type.HOPPER);
            HopperMinecartEntity hopperMinecart = (HopperMinecartEntity) abstractMinecartEntity;

            NbtUtil.readInventoryFromNbt(stack.getNbt(), hopperMinecart);
            if (stack.hasCustomName()) hopperMinecart.setCustomName(stack.getName());

            world.spawnEntity(hopperMinecart);
            stack.decrement(1);
            cir.setReturnValue(stack);
        } else if (stack.getItem() == Items.FURNACE_MINECART) {
            AbstractMinecartEntity abstractMinecartEntity = AbstractMinecartEntity.create(world, d, e + g, f, AbstractMinecartEntity.Type.FURNACE);
            FurnaceMinecartEntity furnaceMinecart = (FurnaceMinecartEntity) abstractMinecartEntity;

            furnaceMinecart.fuel = NbtUtil.getInt(stack.getNbt(), "Fuel", 0, Config.get().maxFurnaceMinecartFuel);
            furnaceMinecart.pushX = furnaceMinecart.getX() - blockPos.getX();
            furnaceMinecart.pushZ = furnaceMinecart.getZ() - blockPos.getZ();
            if (stack.hasCustomName()) furnaceMinecart.setCustomName(stack.getName());

            world.spawnEntity(furnaceMinecart);
            stack.decrement(1);
            cir.setReturnValue(stack);
        }
    }
}
