package me.melontini.andromeda.mixin.entities.better_furnace_minecart;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.better_furnace_minecart.BetterFurnaceMinecart;
import me.melontini.andromeda.util.ItemStackUtil;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.base.util.Support;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;

@Mixin(FurnaceMinecartEntity.class)
@Feature({"betterFurnaceMinecart", "furnaceMinecartTakeFuelWhenLow"})
abstract class FurnaceMinecartIntakeMixin extends AbstractMinecartEntity {
    @Unique
    private static final BetterFurnaceMinecart am$bfm = ModuleManager.quick(BetterFurnaceMinecart.class);

    @Shadow public int fuel;

    protected FurnaceMinecartIntakeMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void andromeda$tick(CallbackInfo ci) {
        if (!am$bfm.config().enabled || !am$bfm.config().takeFuelWhenLow) return;

        if (!this.world.isClient() && this.fuel < 100) {
            if (world.getTime() % 20 == 0) {
                if (Support.getWeak("fabrication", () -> () ->
                        getClass().getField("fabrication$pauseFuel").getInt(this) > 0).orElse(false)) return;

                AbstractMinecartEntity entity = this.world
                        .getEntitiesByClass(AbstractMinecartEntity.class, this.getBoundingBox().expand(1.5, 0, 1.5), minecart -> minecart instanceof Inventory)
                        .stream()
                        .min(Comparator.comparingDouble(value -> value.squaredDistanceTo(this)))
                        .orElse(null);

                if (entity instanceof Inventory inventory) {
                    for (int i = 0; i < inventory.size(); ++i) {
                        ItemStack stack = inventory.getStack(i);
                        if (FuelRegistry.INSTANCE.get(stack.getItem()) != null) {
                            int itemFuel = FuelRegistry.INSTANCE.get(stack.getItem());
                            if ((this.fuel + (itemFuel * 2.25)) <= am$bfm.config().maxFuel) {
                                ItemStack reminder = stack.getRecipeRemainder();
                                if (!reminder.isEmpty())
                                    ItemStackUtil.spawn(entity.getPos(), stack.getRecipeRemainder(), world);
                                stack.decrement(1);

                                this.fuel += (int) (itemFuel * 2.25);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
