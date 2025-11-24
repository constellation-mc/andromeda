package me.melontini.andromeda.modules.entities.minecarts.entities;

import java.util.Optional;
import me.melontini.andromeda.bootstrap.ModuleManager;
import me.melontini.andromeda.modules.entities.minecart_speed_control.MinecartSpeedControl;
import me.melontini.andromeda.modules.entities.minecarts.MinecartEntities;
import me.melontini.andromeda.modules.entities.minecarts.MinecartItems;
import me.melontini.dark_matter.api.base.util.MathUtil;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class AnvilMinecartEntity extends AbstractMinecart {
  public AnvilMinecartEntity(EntityType<? extends AnvilMinecartEntity> entityType, Level world) {
    super(entityType, world);
  }

  public AnvilMinecartEntity(Level world, double x, double y, double z) {
    super(MinecartEntities.ANVIL_MINECART_ENTITY.orThrow(), world, x, y, z);
  }

  @Override
  public Type getMinecartType() {
    return Type.CHEST;
  }

  @Override
  public InteractionResult interact(Player player, InteractionHand hand) {
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  public boolean causeFallDamage(
      float fallDistance, float damageMultiplier, DamageSource damageSource) {
    int i = Mth.ceil(fallDistance - 1.0F);
    if (i >= 0) {
      float f = (float) Math.min(MathUtil.fastFloor(i * 2), 40);
      for (Entity entity : level.getEntitiesOfClass(
          Entity.class, this.getBoundingBox().inflate(0.1), EntitySelector.NO_SPECTATORS)) {
        if (!(entity instanceof AbstractMinecart)) {
          entity.hurt(level.damageSources().anvil(this), f);
        }
      }
    }
    return false;
  }

  @Override
  public Item getDropItem() {
    return MinecartItems.ANVIL_MINECART.orThrow();
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private static final Optional<MinecartSpeedControl> optional =
      ModuleManager.get().get(MinecartSpeedControl.class);

  @Override
  public double getMaxSpeed() {
    double d = (this.isInWater() ? 0.08 : 0.1) / 20.0;
    return optional
        .map(ms -> {
          var c = level.am$get(MinecartSpeedControl.CONFIG);
          return c.available ? d * c.modifier : d;
        })
        .orElse(d);
  }

  @Override
  public BlockState getDefaultDisplayBlockState() {
    return Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.NORTH);
  }

  @Override
  public ItemStack getPickResult() {
    return new ItemStack(MinecartItems.ANVIL_MINECART.orThrow());
  }
}
