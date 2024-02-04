package me.melontini.andromeda.modules.entities.minecarts.entities;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.minecart_speed_control.MinecartSpeedControl;
import me.melontini.andromeda.modules.entities.minecarts.MinecartEntities;
import me.melontini.andromeda.modules.entities.minecarts.MinecartItems;
import me.melontini.dark_matter.api.base.util.MathStuff;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Optional;

public class AnvilMinecartEntity extends AbstractMinecartEntity {
    public AnvilMinecartEntity(EntityType<? extends AnvilMinecartEntity> entityType, World world) {
        super(entityType, world);
    }

    public AnvilMinecartEntity(World world, double x, double y, double z) {
        super(MinecartEntities.ANVIL_MINECART_ENTITY.orThrow(), world, x, y, z);
    }

    @Override
    public Type getMinecartType() {
        return Type.CHEST;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        return ActionResult.success(world.isClient);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        int i = MathHelper.ceil(fallDistance - 1.0F);
        if (i >= 0) {
            float f = (float) Math.min(MathStuff.fastFloor(i * 2), 40);
            for (Entity entity : world.getEntitiesByClass(Entity.class, this.getBoundingBox().expand(0.1), EntityPredicates.EXCEPT_SPECTATOR)) {
                if (!(entity instanceof AbstractMinecartEntity)) {
                    entity.damage(world.getDamageSources().fallingAnvil(this), f);
                }
            }

        }
        return false;
    }

    @Override
    public Item asItem() {
        return MinecartItems.ANVIL_MINECART.orThrow();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<MinecartSpeedControl> optional = ModuleManager.get().getModule(MinecartSpeedControl.class);

    @Override
    public double getMaxSpeed() {
        double d = (this.isTouchingWater() ? 0.08 : 0.1) / 20.0;
        return optional.map(ms -> d * world.am$get(ms).modifier).orElse(d);
    }

    @Override
    public BlockState getDefaultContainedBlock() {
        return Blocks.ANVIL.getDefaultState().with(AnvilBlock.FACING, Direction.NORTH);
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(MinecartItems.ANVIL_MINECART.orThrow());
    }
}
