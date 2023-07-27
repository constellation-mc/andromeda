package me.melontini.andromeda.mixin.entities.furnace_minecart;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.ItemStackUtil;
import me.melontini.andromeda.util.SharedConstants;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
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
        if (!this.world.isClient() && Andromeda.CONFIG.betterFurnaceMinecart && Andromeda.CONFIG.furnaceMinecartTakeFuelWhenLow && this.fuel < 100) {
            if (world.getTime() % 20 == 0) {
                if (SharedConstants.FABRICATION_LOADED) {
                    try {
                        if (getClass().getField("fabrication$pauseFuel").getInt(this) > 0) return;
                    } catch (IllegalAccessException | NoSuchFieldException ignored) {}
                }

                AbstractMinecartEntity entity = this.world
                        .getEntitiesByClass(AbstractMinecartEntity.class, this.getBoundingBox().expand(1.5, 0, 1.5), minecart -> minecart instanceof Inventory)
                        .stream()
                        .min(Comparator.comparingDouble(value -> value.squaredDistanceTo(this)))
                        .orElse(null);

                if (entity instanceof Inventory inventory) {
                    for (int i = 0; i < inventory.size(); ++i) {
                        ItemStack stack = inventory.getStack(i);
                        if (FuelRegistryImpl.INSTANCE.get(stack.getItem()) != null) {
                            int itemFuel = FuelRegistryImpl.INSTANCE.get(stack.getItem());
                            if ((this.fuel + (itemFuel * 2.25)) <= Andromeda.CONFIG.maxFurnaceMinecartFuel) {
                                if (stack.getItem().getRecipeRemainder() != null)
                                    ItemStackUtil.spawn(entity.getPos(), stack.getItem().getRecipeRemainder().getDefaultStack(), world);
                                stack.decrement(1);

                                this.fuel += (itemFuel * 2.25);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
