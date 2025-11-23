package me.melontini.andromeda.modules.mechanics.throwable_items;

import static me.melontini.andromeda.common.Andromeda.id;
import static me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager.RELOADER;

import me.melontini.andromeda.common.Andromeda;
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
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;

public final class Main {

  public static final Keeper<EntityType<FlyingItemEntity>> FLYING_ITEM = Keeper.create();

  public static final ResourceKey<DamageType> BRICKED =
      Andromeda.key(Registries.DAMAGE_TYPE, "bricked");

  public static final ResourceLocation FLYING_STACK_LANDED = Andromeda.id("flying_stack_landed");
  public static final ResourceLocation ITEMS_WITH_BEHAVIORS = Andromeda.id("items_with_behaviors");
  public static final ResourceLocation COLORED_FLYING_STACK_LANDED =
      Andromeda.id("colored_flying_stack_landed");

  public static final AbstractProjectileDispenseBehavior BEHAVIOR = new AbstractProjectileDispenseBehavior() {
    @Override
    protected Projectile getProjectile(Level world, Position position, ItemStack stack) {
      ItemStack stack1 = stack.copy();
      stack1.setCount(1);
      return new FlyingItemEntity(stack1, position.x(), position.y(), position.z(), world);
    }
  };

  public static final Keeper<LootContextParamSet> CONTEXT_TYPE = Keeper.create();
  public static final Keeper<CommandType> PARTICLE_COMMAND = Keeper.create();
  public static final Keeper<CommandType> ITEM_PLOP_COMMAND = Keeper.create();

  static void init() {
    FLYING_ITEM.init(RegistryUtil.register(
        BuiltInRegistries.ENTITY_TYPE,
        id("flying_item"),
        () -> FabricEntityTypeBuilder.<FlyingItemEntity>create(
                MobCategory.MISC, FlyingItemEntity::new)
            .dimensions(new EntityDimensions(0.25F, 0.25F, true))
            .trackRangeChunks(4)
            .trackedUpdateRate(10)
            .build()));

    CONTEXT_TYPE.init(LootContextParamSets.register("andromeda:throwable_items", builder -> builder
        .required(LootContextParams.ORIGIN)
        .required(LootContextParams.DIRECT_KILLER_ENTITY)
        .required(LootContextParams.TOOL)
        .optional(LootContextParams.KILLER_ENTITY)
        .optional(LootContextParams.THIS_ENTITY)
        .optional(LootContextParams.BLOCK_STATE)
        .optional(LootContextParams.BLOCK_ENTITY)));
    PARTICLE_COMMAND.init(CommandType.register(id("particles"), ParticleCommand.CODEC));
    ITEM_PLOP_COMMAND.init(CommandType.register(id("item_plop"), ItemPlopEffect.CODEC));

    ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
      var packet = sendItemsS2CPacket(server.dm$getReloader(RELOADER));
      sender.sendPacket(ITEMS_WITH_BEHAVIORS, packet);
    });
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
      var packet = sendItemsS2CPacket(server.dm$getReloader(RELOADER));
      for (ServerPlayer player : PlayerLookup.all(server)) {
        ServerPlayNetworking.send(player, ITEMS_WITH_BEHAVIORS, packet);
      }
    });

    ServerReloadersEvent.EVENT.register(context -> context.register(new ItemBehaviorManager()));

    DefaultBehaviors.init();
  }

  private static FriendlyByteBuf sendItemsS2CPacket(ItemBehaviorManager manger) {
    var items = manger.itemsWithBehaviors();
    var packet = PacketByteBufs.create().writeVarInt(items.size());
    for (Item item : items) {
      packet.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item));
    }
    return packet;
  }

  public enum Event {
    BLOCK,
    ENTITY,
    MISS,
    ANY
  }
}
