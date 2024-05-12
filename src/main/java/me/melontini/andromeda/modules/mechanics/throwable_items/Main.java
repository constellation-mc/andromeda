package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.DefaultBehaviors;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemPlopEffect;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ParticleCommand;
import me.melontini.commander.api.command.CommandType;
import me.melontini.dark_matter.api.data.loading.ServerReloadersEvent;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

import static me.melontini.andromeda.common.Andromeda.id;
import static me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager.RELOADER;
import static me.melontini.andromeda.util.CommonValues.MODID;

public final class Main implements ServerReloadersEvent {

    public static final Keeper<EntityType<FlyingItemEntity>> FLYING_ITEM = Keeper.create();

    public static final RegistryKey<DamageType> BRICKED = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id("bricked"));

    public static final Identifier FLYING_STACK_LANDED = new Identifier(MODID, "flying_stack_landed");
    public static final Identifier ITEMS_WITH_BEHAVIORS = new Identifier(MODID, "items_with_behaviors");
    public static final Identifier COLORED_FLYING_STACK_LANDED = new Identifier(MODID, "colored_flying_stack_landed");

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

    Main() {
        FLYING_ITEM.init(RegistryUtil.register(Registries.ENTITY_TYPE, id("flying_item"), () -> FabricEntityTypeBuilder.<FlyingItemEntity>create(SpawnGroup.MISC, FlyingItemEntity::new)
                .dimensions(new EntityDimensions(0.25F, 0.25F, true))
                .trackRangeChunks(4).trackedUpdateRate(10).build()));

        CONTEXT_TYPE.init(LootContextTypes.register("andromeda:throwable_items", builder -> builder
                .require(LootContextParameters.ORIGIN).require(LootContextParameters.DIRECT_KILLER_ENTITY)
                .require(LootContextParameters.TOOL).allow(LootContextParameters.KILLER_ENTITY)
                .allow(LootContextParameters.THIS_ENTITY).allow(LootContextParameters.BLOCK_STATE)
                .allow(LootContextParameters.BLOCK_ENTITY)));
        PARTICLE_COMMAND.init(CommandType.register(id("particles"), ParticleCommand.CODEC));
        ITEM_PLOP_COMMAND.init(CommandType.register(id("item_plop"), ItemPlopEffect.CODEC));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var packet = sendItemsS2CPacket(server.dm$getReloader(RELOADER));
            sender.sendPacket(ITEMS_WITH_BEHAVIORS, packet);
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            var packet = sendItemsS2CPacket(server.dm$getReloader(RELOADER));
            for (ServerPlayerEntity player : PlayerLookup.all(server)) {
                ServerPlayNetworking.send(player, ITEMS_WITH_BEHAVIORS, packet);
            }
        });

        ServerReloadersEvent.EVENT.register(this);

        DefaultBehaviors.init();
    }

    private static PacketByteBuf sendItemsS2CPacket(ItemBehaviorManager manger) {
        var items = manger.itemsWithBehaviors();
        var packet = PacketByteBufs.create().writeVarInt(items.size());
        for (Item item : items) {
            packet.writeIdentifier(Registries.ITEM.getId(item));
        }
        return packet;
    }

    @Override
    public void onServerReloaders(Context context) {
        context.register(new ItemBehaviorManager());
    }

    public enum Event {
        BLOCK, ENTITY, MISS, ANY
    }
}
