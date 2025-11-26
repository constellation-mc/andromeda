package dev.zenfyr.andromeda.modules.entities.better_furnace_minecart.mixin;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.entities.better_furnace_minecart.BetterFurnaceMinecart;
import dev.zenfyr.pulsar.itemstack.ItemStackUtil;
import dev.zenfyr.pulsar.reflection.Reflect;
import dev.zenfyr.pulsar.util.ExceptionUtil;
import dev.zenfyr.pulsar.util.SupportUtil;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Optional;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartFurnace.class)
abstract class FurnaceMinecartIntakeMixin extends AbstractMinecart {

  // stfu IDEA.
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Unique private static final Optional<Field> fb$pauseFuel = SupportUtil.fallback(
      "fabrication",
      () -> Reflect.findField(MinecartFurnace.class, "fabrication$pauseFuel"),
      Optional::empty);

  @Shadow
  public int fuel;

  protected FurnaceMinecartIntakeMixin(EntityType<?> entityType, Level world) {
    super(entityType, world);
  }

  @Inject(at = @At("HEAD"), method = "tick")
  private void andromeda$tick(CallbackInfo ci) {
    if (!Andromeda.MAIN.get(BetterFurnaceMinecart.CONFIG).takeFuelWhenLow) return;

    if (!this.level.isClientSide() && this.fuel < 100) {
      if (level.getGameTime() % 20 == 0) {
        if (fb$pauseFuel
            .map(f -> ExceptionUtil.supply(() -> f.getInt(this)) > 0)
            .orElse(false)) return;

        AbstractMinecart entity = this.level
            .getEntitiesOfClass(
                AbstractMinecart.class,
                this.getBoundingBox().inflate(1.5, 0, 1.5),
                Container.class::isInstance)
            .stream()
            .min(Comparator.comparingDouble(value -> value.distanceToSqr(this)))
            .orElse(null);

        if (entity instanceof Container inventory) {
          for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (FuelRegistry.INSTANCE.get(stack.getItem()) != null) {
              int itemFuel = FuelRegistry.INSTANCE.get(stack.getItem());
              if ((this.fuel + (itemFuel * 2.25))
                  <= Andromeda.MAIN.get(BetterFurnaceMinecart.CONFIG).maxFuel) {
                ItemStack reminder = stack.getRecipeRemainder();
                if (!reminder.isEmpty())
                  ItemStackUtil.spawn(entity.position(), stack.getRecipeRemainder(), level);
                stack.shrink(1);

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
