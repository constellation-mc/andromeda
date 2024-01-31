package me.melontini.andromeda.modules.entities.boats.items;

import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.stat.Stats;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;
import java.util.function.Predicate;

public class AndromedaBoatItem<T extends BoatEntity> extends Item {

    private static final Predicate<Entity> RIDERS = EntityPredicates.EXCEPT_SPECTATOR.and(Entity::canHit);
    private final BoatEntity.Type type;
    private final Keeper<EntityType<T>> keeper;

    public AndromedaBoatItem(Keeper<EntityType<T>> keeper, BoatEntity.Type type, Settings settings) {
        super(settings);
        this.keeper = keeper;
        this.type = type;
        DispenserBlock.registerBehavior(this, new BoatDispenseBehavior());
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.ANY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(itemStack);
        } else {
            Vec3d vec3d = user.getRotationVec(1.0F);
            List<Entity> list = world.getOtherEntities(user, user.getBoundingBox().stretch(vec3d.multiply(5.0)).expand(1.0), RIDERS);
            if (!list.isEmpty()) {
                Vec3d vec3d2 = user.getEyePos();

                for (Entity entity : list) {
                    Box box = entity.getBoundingBox().expand(entity.getTargetingMargin());
                    if (box.contains(vec3d2)) {
                        return TypedActionResult.pass(itemStack);
                    }
                }
            }

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                T furnace = MakeSure.notNull(this.keeper.orThrow().create(world));
                furnace.setPosition(hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);

                furnace.setBoatType(this.type);
                furnace.setYaw(user.getYaw());
                if (!world.isSpaceEmpty(furnace, furnace.getBoundingBox())) {
                    return TypedActionResult.fail(itemStack);
                } else {
                    if (!world.isClient) {
                        world.spawnEntity(furnace);
                        world.emitGameEvent(user, GameEvent.ENTITY_PLACE, new BlockPos(hitResult.getPos()));
                        if (!user.getAbilities().creativeMode) {
                            itemStack.decrement(1);
                        }
                    }

                    user.incrementStat(Stats.USED.getOrCreateStat(this));
                    return TypedActionResult.success(itemStack, world.isClient());
                }
            } else {
                return TypedActionResult.pass(itemStack);
            }
        }
    }

    private class BoatDispenseBehavior extends ItemDispenserBehavior {

        private final ItemDispenserBehavior itemDispenser;

        public BoatDispenseBehavior() {
            this.itemDispenser = new ItemDispenserBehavior();
        }

        @Override
        protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
            World world = pointer.getWorld();
            double d = 0.5625 + EntityType.BOAT.getWidth() / 2.0;
            double e = pointer.getX() + direction.getOffsetX() * d;
            double f = pointer.getY() + direction.getOffsetY() * 1.125F;
            double g = pointer.getZ() + direction.getOffsetZ() * d;
            BlockPos blockPos = pointer.getPos().offset(direction);
            double h;
            if (world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
                h = 1.0;
            } else {
                if (!world.getBlockState(blockPos).isAir() || !world.getFluidState(blockPos.down()).isIn(FluidTags.WATER)) {
                    return this.itemDispenser.dispense(pointer, stack);
                }
                h = 0.0;
            }

            T boatEntity = MakeSure.notNull(AndromedaBoatItem.this.keeper.orThrow().create(world));
            boatEntity.setPosition(e, f + h, g);

            boatEntity.setBoatType(AndromedaBoatItem.this.type);
            boatEntity.setYaw(direction.asRotation());

            world.spawnEntity(boatEntity);
            stack.decrement(1);
            return stack;
        }
    }
}
