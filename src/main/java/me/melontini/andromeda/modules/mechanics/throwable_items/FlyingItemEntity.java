package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.Event;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.EventType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class FlyingItemEntity extends ThrownItemEntity {

    private final List<Event> behaviors;

    public FlyingItemEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
        this.setItem(ItemStack.EMPTY);
        this.behaviors = Collections.emptyList();
    }

    public FlyingItemEntity(ItemStack stack, double d, double e, double f, World world) {
        super(Main.FLYING_ITEM.orThrow(), d, e, f, world);
        this.setItem(stack);
        if (world.isClient()) {
            this.behaviors = Collections.emptyList();
            return;
        }
        this.behaviors = ItemBehaviorManager.get(world.getServer()).getBehaviors(getItem().getItem(), EventType.TICK);
    }

    public FlyingItemEntity(ItemStack stack, Entity entity, World world) {
        super(Main.FLYING_ITEM.orThrow(), world);
        this.setOwner(entity);
        this.setItem(stack);
        if (world.isClient()) {
            this.behaviors = Collections.emptyList();
            return;
        }
        this.behaviors = ItemBehaviorManager.get(world.getServer()).getBehaviors(getItem().getItem(), EventType.TICK);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.world.isClient() || this.behaviors.isEmpty()) return;

        var context = makeContext(null);
        this.behaviors.forEach(event -> event.run(context));
    }

    public void onThrow() {
        if (this.world.isClient()) return;
        var events = ItemBehaviorManager.get(world.getServer()).getBehaviors(getItem().getItem(), EventType.THROW);
        if (events.isEmpty()) return;

        var context = makeContext(null);
        events.forEach(event -> {
            if (!this.isRemoved()) event.run(context);
        });
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        if (!this.world.isClient()) {
            var manager = ItemBehaviorManager.get(world.getServer());

            List<Event> events = switch (hitResult.getType()) {
                case BLOCK -> manager.getBehaviors(getItem().getItem(), EventType.BLOCK);
                case ENTITY -> manager.getBehaviors(getItem().getItem(), EventType.ENTITY);
                case MISS ->  manager.getBehaviors(getItem().getItem(), EventType.MISS);
            };
            var stream = Stream.concat(events.stream(), manager.getBehaviors(getItem().getItem(), EventType.ANY).stream()).toList();

            if (!stream.isEmpty()) {
                var context = makeContext(hitResult);
                stream.forEach(event -> {
                    if (!this.isRemoved()) event.run(context);
                });
            }
        }
        this.discard();
    }

    private Context makeContext(@Nullable HitResult hitResult) {
        LootContextParameterSet.Builder set = new LootContextParameterSet.Builder((ServerWorld) world);
        if (getOwner() != null) set.add(LootContextParameters.KILLER_ENTITY, getOwner());
        set.add(LootContextParameters.DIRECT_KILLER_ENTITY, this);
        set.add(LootContextParameters.TOOL, getItem());
        set.add(LootContextParameters.ORIGIN, this.getPos());

        if (hitResult != null) {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                set.add(LootContextParameters.BLOCK_STATE, world.getBlockState(blockHitResult.getBlockPos()));
                BlockEntity blockEntity = world.getBlockEntity(blockHitResult.getBlockPos());
                if (blockEntity != null) set.add(LootContextParameters.BLOCK_ENTITY, blockEntity);
            }

            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                set.add(LootContextParameters.THIS_ENTITY, entityHitResult.getEntity());
            }
        }

        LootContext context = new LootContext.Builder(set.build(Main.ITEM_CONTEXT.orThrow())).build(null);
        return new Context(getItem(), this, (ServerWorld) world, getOwner(), hitResult, context);
    }

    @Override
    protected Item getDefaultItem() {
        return getItem().getItem();
    }
}
