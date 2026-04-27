package dev.zenfyr.andromeda.modules.entities.boats.items;

import dev.zenfyr.andromeda.common.util.MiscUtil;
import dev.zenfyr.andromeda.modules.entities.boats.entities.BoatEntityWithBlock;
import dev.zenfyr.pulsar.util.MakeSure;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AndromedaBoatItem<T extends BoatEntityWithBlock> extends Item {

  private static final Predicate<Entity> RIDERS =
      EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
  private final Supplier<EntityType<?>> keeper;

  public AndromedaBoatItem(Supplier<EntityType<?>> keeper, Properties settings) {
    super(settings);
    this.keeper = keeper;
    DispenserBlock.registerBehavior(this, new BoatDispenseBehavior());
  }

  @Override
  public InteractionResult use(Level world, Player user, InteractionHand hand) {
    ItemStack itemStack = user.getItemInHand(hand);
    HitResult hitResult = getPlayerPOVHitResult(world, user, ClipContext.Fluid.ANY);
    if (hitResult.getType() == HitResult.Type.MISS) {
      return InteractionResult.PASS;
    } else {
      Vec3 vec3d = user.getViewVector(1.0F);
      List<Entity> list = world.getEntities(
          user, user.getBoundingBox().expandTowards(vec3d.scale(5.0)).inflate(1.0), RIDERS);
      if (!list.isEmpty()) {
        Vec3 vec3d2 = user.getEyePosition();

        for (Entity entity : list) {
          AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
          if (box.contains(vec3d2)) {
            return InteractionResult.PASS;
          }
        }
      }

      if (hitResult.getType() == HitResult.Type.BLOCK) {
        T furnace =
            (T) MakeSure.notNull(this.keeper.get().create(world, EntitySpawnReason.DISPENSER));
        furnace.setPos(
            hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);

        furnace.setYRot(user.getYRot());
        if (!world.noCollision(furnace, furnace.getBoundingBox())) {
          return InteractionResult.FAIL;
        } else {
          if (!world.isClientSide()) {
            world.addFreshEntity(furnace);
            world.gameEvent(
                user, GameEvent.ENTITY_PLACE, MiscUtil.vec3dAsBlockPos(hitResult.getLocation()));
            if (!user.getAbilities().instabuild) {
              itemStack.shrink(1);
            }
          }

          user.awardStat(Stats.ITEM_USED.get(this));
          return InteractionResult.SUCCESS;
        }
      } else {
        return InteractionResult.PASS;
      }
    }
  }

  private class BoatDispenseBehavior extends DefaultDispenseItemBehavior {

    private final DefaultDispenseItemBehavior itemDispenser;

    public BoatDispenseBehavior() {
      this.itemDispenser = new DefaultDispenseItemBehavior();
    }

    @Override
    protected ItemStack execute(BlockSource pointer, ItemStack stack) {
      Direction direction = pointer.state().getValue(DispenserBlock.FACING);
      ServerLevel world = pointer.level();
      double d = 0.5625 + AndromedaBoatItem.this.keeper.get().getWidth() / 2.0;
      double e = pointer.center().x() + direction.getStepX() * d;
      double f = pointer.center().y() + direction.getStepY() * 1.125F;
      double g = pointer.center().z() + direction.getStepZ() * d;
      BlockPos blockPos = pointer.pos().relative(direction);
      double h;
      if (world.getFluidState(blockPos).is(FluidTags.WATER)) {
        h = 1.0;
      } else {
        if (!world.getBlockState(blockPos).isAir()
            || !world.getFluidState(blockPos.below()).is(FluidTags.WATER)) {
          return this.itemDispenser.dispense(pointer, stack);
        }
        h = 0.0;
      }

      T boatEntity = (T) MakeSure.notNull(
          AndromedaBoatItem.this.keeper.get().create(world, EntitySpawnReason.DISPENSER));
      boatEntity.setPos(e, f + h, g);

      boatEntity.setYRot(direction.toYRot());

      world.addFreshEntity(boatEntity);
      stack.shrink(1);
      return stack;
    }
  }
}
