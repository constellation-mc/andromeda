package me.melontini.andromeda.modules.mechanics.throwable_items;

import com.google.common.collect.ImmutableList;
import static me.melontini.andromeda.common.Andromeda.id;
import static me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager.RELOADER;

import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.DefaultBehaviors;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemPlopEffect;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ParticleCommand;
import me.melontini.andromeda.modules.mechanics.throwable_items.packets.ColoredStackLandedPayload;
import me.melontini.andromeda.modules.mechanics.throwable_items.packets.FlyingStackLandedPayload;
import me.melontini.andromeda.modules.mechanics.throwable_items.packets.ItemBehaviorsPayload;
import me.melontini.commander.api.command.CommandType;
import me.melontini.dark_matter.api.data.loading.ServerReloadersEvent;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class Main {

  public static final Keeper<EntityType<FlyingItemEntity>> FLYING_ITEM = Keeper.create();

  public static final RegistryKey<DamageType> BRICKED =
      Andromeda.key(RegistryKeys.DAMAGE_TYPE, "bricked");

  public static final Identifier FLYING_STACK_LANDED = Andromeda.id("flying_stack_landed");
  public static final Identifier ITEMS_WITH_BEHAVIORS = Andromeda.id("items_with_behaviors");
  public static final Identifier COLORED_FLYING_STACK_LANDED =
      Andromeda.id("colored_flying_stack_landed");

  public static final ItemDispenserBehavior BEHAVIOR = new ItemDispenserBehavior() {

    private ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
            ItemStack stack1 = stack.copy();
            stack1.setCount(1);
            return new FlyingItemEntity(stack1, position.getX(), position.getY(), position.getZ(), world);
        }

        @Override
        protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            World world = pointer.world();
            Direction direction = pointer.state().get(DispenserBlock.FACING);
            Position position = DispenserBlock.getOutputLocation(pointer, 0.7, new Vec3d(0.0, 0.1, 0.0));
            ProjectileEntity projectileEntity = this.createProjectile(world, position, stack);
            projectileEntity.setVelocity(projectileEntity, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ(), 1.1f, 6f);
            world.spawnEntity(projectileEntity);
            stack.decrement(1);
            return stack;
        }

        protected void playSound(BlockPointer pointer) {
            pointer.world().syncWorldEvent(1002, pointer.pos(), 0);
        }
    };

  public static final Keeper<LootContextType> CONTEXT_TYPE = Keeper.create();
  public static final Keeper<CommandType> PARTICLE_COMMAND = Keeper.create();
  public static final Keeper<CommandType> ITEM_PLOP_COMMAND = Keeper.create();

  static void init() {
    FLYING_ITEM.init(RegistryUtil.register(
        Registries.ENTITY_TYPE,
        id("flying_item"),
        () -> FabricEntityTypeBuilder.<FlyingItemEntity>create(
                SpawnGroup.MISC, FlyingItemEntity::new)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
            .trackRangeChunks(4)
            .trackedUpdateRate(10)
            .build()));

    CONTEXT_TYPE.init(LootContextTypes.register("andromeda:throwable_items", builder -> builder
        .require(LootContextParameters.ORIGIN)
        .require(LootContextParameters.DIRECT_ATTACKING_ENTITY)
        .require(LootContextParameters.TOOL)
        .allow(LootContextParameters.ATTACKING_ENTITY)
        .allow(LootContextParameters.THIS_ENTITY)
        .allow(LootContextParameters.BLOCK_STATE)
        .allow(LootContextParameters.BLOCK_ENTITY)));
    PARTICLE_COMMAND.init(CommandType.register(id("particles"), ParticleCommand.CODEC));
    ITEM_PLOP_COMMAND.init(CommandType.register(id("item_plop"), ItemPlopEffect.CODEC));

    PayloadTypeRegistry.playS2C().register(ItemBehaviorsPayload.ID, ItemBehaviorsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ColoredStackLandedPayload.ID, ColoredStackLandedPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FlyingStackLandedPayload.ID, FlyingStackLandedPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            sender.sendPacket(new ItemBehaviorsPayload(ImmutableList.copyOf(server.dm$getReloader(RELOADER).itemsWithBehaviors())));
    });
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
      var packet = new ItemBehaviorsPayload(ImmutableList.copyOf(server.dm$getReloader(RELOADER).itemsWithBehaviors()));
      for (ServerPlayerEntity player : PlayerLookup.all(server)) {
        ServerPlayNetworking.send(player, packet);
            }
        });

    ServerReloadersEvent.EVENT.register(context -> context.register(new ItemBehaviorManager()));

    DefaultBehaviors.init();
  }

    public enum Event {
        BLOCK, ENTITY, MISS, ANY
    }
}
