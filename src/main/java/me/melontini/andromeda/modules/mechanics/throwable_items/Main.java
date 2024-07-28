package me.melontini.andromeda.modules.mechanics.throwable_items;

import com.google.common.collect.ImmutableList;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.DefaultBehaviors;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemPlopEffect;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ParticleCommand;
import me.melontini.andromeda.modules.mechanics.throwable_items.packets.ItemBehaviorsPayload;
import me.melontini.commander.api.command.CommandType;
import me.melontini.dark_matter.api.data.loading.ServerReloadersEvent;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
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
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

import static me.melontini.andromeda.common.Andromeda.id;
import static me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager.RELOADER;

public final class Main {

    public static final Keeper<EntityType<FlyingItemEntity>> FLYING_ITEM = Keeper.create();

    public static final RegistryKey<DamageType> BRICKED = Andromeda.key(RegistryKeys.DAMAGE_TYPE, "bricked");

    public static final Identifier FLYING_STACK_LANDED = Andromeda.id("flying_stack_landed");
    public static final Identifier ITEMS_WITH_BEHAVIORS = Andromeda.id("items_with_behaviors");
    public static final Identifier COLORED_FLYING_STACK_LANDED = Andromeda.id("colored_flying_stack_landed");

    public static final ProjectileDispenserBehavior BEHAVIOR = new ProjectileDispenserBehavior() {
        @Override
        protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
            ItemStack stack1 = stack.copy();
            stack1.setCount(1);
            return new FlyingItemEntity(stack1, position.getX(), position.getY(), position.getZ(), world);
        }
    };

    public static final Keeper<LootContextType> CONTEXT_TYPE = Keeper.create();
    public static final Keeper<CommandType> PARTICLE_COMMAND = Keeper.create();
    public static final Keeper<CommandType> ITEM_PLOP_COMMAND = Keeper.create();

    static void init() {
        FLYING_ITEM.init(RegistryUtil.register(Registries.ENTITY_TYPE, id("flying_item"), () -> FabricEntityTypeBuilder.<FlyingItemEntity>create(SpawnGroup.MISC, FlyingItemEntity::new)
                .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                .trackRangeChunks(4).trackedUpdateRate(10).build()));

        CONTEXT_TYPE.init(LootContextTypes.register("andromeda:throwable_items", builder -> builder
                .require(LootContextParameters.ORIGIN).require(LootContextParameters.DIRECT_ATTACKING_ENTITY)
                .require(LootContextParameters.TOOL).allow(LootContextParameters.ATTACKING_ENTITY)
                .allow(LootContextParameters.THIS_ENTITY).allow(LootContextParameters.BLOCK_STATE)
                .allow(LootContextParameters.BLOCK_ENTITY)));
        PARTICLE_COMMAND.init(CommandType.register(id("particles"), ParticleCommand.CODEC));
        ITEM_PLOP_COMMAND.init(CommandType.register(id("item_plop"), ItemPlopEffect.CODEC));

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
