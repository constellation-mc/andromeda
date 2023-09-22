package me.melontini.andromeda.mixin.entities.furnace_minecart;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.ItemStackUtil;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;

@Mixin(FurnaceMinecartEntity.class)
@MixinRelatedConfigOption({"betterFurnaceMinecart", "furnaceMinecartTakeFuelWhenLow"})
public abstract class FurnaceMinecartIntakeMixin extends AbstractMinecartEntity {
    @Shadow
    public int fuel;

    protected FurnaceMinecartIntakeMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void andromeda$tick(CallbackInfo ci) {
        if (!Config.get().betterFurnaceMinecart || !Config.get().furnaceMinecartTakeFuelWhenLow) return;

        if (!this.world.isClient() && this.fuel < 100) {
            if (world.getTime() % 20 == 0) {
                if (Utilities.ifLoadedWeak("fabrication", () -> getClass().getField("fabrication$pauseFuel").getInt(this) > 0).orElse(false)) return;

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
                            if ((this.fuel + (itemFuel * 2.25)) <= Config.get().maxFurnaceMinecartFuel) {
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
