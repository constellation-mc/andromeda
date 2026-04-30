package dev.zenfyr.andromeda.modules.entities.minecarts;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.AndromedaItemGroup;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.andromeda.modules.entities.minecarts.entities.AnvilMinecartEntity;
import dev.zenfyr.andromeda.modules.entities.minecarts.items.AndromedaMinecartItem;
import dev.zenfyr.andromeda.modules.entities.minecarts.items.JukeboxMinecartItem;
import dev.zenfyr.andromeda.modules.entities.minecarts.items.NoteBlockMinecartItem;
import dev.zenfyr.andromeda.modules.entities.minecarts.items.SpawnerMinecartItem;
import dev.zenfyr.andromeda.modules.items.minecart_block_picking.MinecartBlockPicking;
import dev.zenfyr.andromeda.modules.items.minecart_block_picking.PickUpBehaviorHandler;
import dev.zenfyr.pulsar.nbt.CompoundTagBuilder;
import dev.zenfyr.pulsar.util.MakeSure;
import java.util.List;
import java.util.Objects;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class MinecartItems {

  public static final Keeper<SpawnerMinecartItem> SPAWNER_MINECART = Keeper.create();
  public static final Keeper<AndromedaMinecartItem<AnvilMinecartEntity>> ANVIL_MINECART =
      Keeper.create();
  public static final Keeper<NoteBlockMinecartItem> NOTE_BLOCK_MINECART = Keeper.create();
  public static final Keeper<JukeboxMinecartItem> JUKEBOX_MINECART = Keeper.create();

  public static void init() {
    var module = ModuleManager.get().get(Minecarts.class).orElseThrow();
    var config = Andromeda.MAIN.get(Minecarts.MAIN_CONFIG);

    if (config.isSpawnerMinecartOn) {
      var key = Andromeda.key(Registries.ITEM, "spawner_minecart");
      SPAWNER_MINECART.init(Registry.register(
          BuiltInRegistries.ITEM,
          key,
          new SpawnerMinecartItem(new Item.Properties().setId(key).stacksTo(1))));
    }

    if (config.isAnvilMinecartOn) {
      var key = Andromeda.key(Registries.ITEM, "anvil_minecart");
      ANVIL_MINECART.init(Registry.register(
          BuiltInRegistries.ITEM,
          key,
          new AndromedaMinecartItem<>(
              MinecartEntities.ANVIL_MINECART_ENTITY,
              new Item.Properties().setId(key).stacksTo(1))));
    }

    if (config.isNoteBlockMinecartOn) {
      var key = Andromeda.key(Registries.ITEM, "note_block_minecart");
      NOTE_BLOCK_MINECART.init(Registry.register(
          BuiltInRegistries.ITEM,
          key,
          new NoteBlockMinecartItem(new Item.Properties().setId(key).stacksTo(1))));
    }

    if (config.isJukeboxMinecartOn) {
      var key = Andromeda.key(Registries.ITEM, "jukebox_minecart");
      JUKEBOX_MINECART.init(Registry.register(
          BuiltInRegistries.ITEM,
          key,
          new JukeboxMinecartItem(new Item.Properties().setId(key).stacksTo(1))));
    }

    var l = List.of(SPAWNER_MINECART, ANVIL_MINECART, NOTE_BLOCK_MINECART, JUKEBOX_MINECART);
    AndromedaItemGroup.BUS.listen(
        acceptor -> acceptor.keepers(module, CreativeModeTabs.TOOLS_AND_UTILITIES, List.copyOf(l)));

    if (ModuleManager.get().get(MinecartBlockPicking.class).isPresent()) {
      if (SPAWNER_MINECART.isPresent()) {
        PickUpBehaviorHandler.registerPickUpBehavior(Blocks.SPAWNER, (state, world, pos) -> {
          if (world.am$get(MinecartBlockPicking.CONFIG).spawnerPicking) {
            SpawnerBlockEntity mobSpawnerBlockEntity = (SpawnerBlockEntity) MakeSure.notNull(
                world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
            ItemStack spawnerMinecart = new ItemStack(SPAWNER_MINECART.orThrow(), 1);
            var nbt = CompoundTagBuilder.create()
                .putString("Entity", String.valueOf(andromeda$getEntityId(mobSpawnerBlockEntity)))
                .build();
            spawnerMinecart.set(
                DataComponents.ENTITY_DATA, TypedEntityData.of(EntityType.SPAWNER_MINECART, nbt));
            return spawnerMinecart;
          }
          return null;
        });
      }

      if (ANVIL_MINECART.isPresent()) {
        PickUpBehaviorHandler.registerPickUpBehavior(
            Blocks.ANVIL, (state, world, pos) -> new ItemStack(ANVIL_MINECART.orThrow()));
      }

      if (NOTE_BLOCK_MINECART.isPresent()) {
        PickUpBehaviorHandler.registerPickUpBehavior(Blocks.NOTE_BLOCK, (state, world, pos) -> {
          NoteBlock noteBlock = (NoteBlock) state.getBlock();
          int noteProp = noteBlock.withPropertiesOf(state).getValue(BlockStateProperties.NOTE);
          ItemStack noteBlockMinecart = new ItemStack(NOTE_BLOCK_MINECART.orThrow());

          var nbt = CompoundTagBuilder.create().putInt("Note", noteProp).build();
          noteBlockMinecart.set(
              DataComponents.ENTITY_DATA,
              TypedEntityData.of(MinecartEntities.NOTEBLOCK_MINECART_ENTITY.orThrow(), nbt));
          return noteBlockMinecart;
        });
      }

      if (JUKEBOX_MINECART.isPresent()) {
        PickUpBehaviorHandler.registerPickUpBehavior(Blocks.JUKEBOX, (state, world, pos) -> {
          JukeboxBlockEntity jukeboxBlockEntity = (JukeboxBlockEntity) MakeSure.notNull(
              world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));

          ItemStack record = jukeboxBlockEntity.getTheItem();
          ItemStack jukeboxMinecart = new ItemStack(JUKEBOX_MINECART.orThrow());

          if (!record.isEmpty()) {
            world.levelEvent(LevelEvent.SOUND_PLAY_JUKEBOX_SONG, pos, 0);
            var nbt = CompoundTagBuilder.create()
                .put(
                    "Items",
                    ItemStack.OPTIONAL_CODEC
                        .encodeStart(NbtOps.INSTANCE, record)
                        .getOrThrow())
                .build();
            jukeboxMinecart.set(
                DataComponents.ENTITY_DATA,
                TypedEntityData.of(MinecartEntities.JUKEBOX_MINECART_ENTITY.orThrow(), nbt));
            jukeboxBlockEntity.setTheItem(ItemStack.EMPTY);
          }
          return jukeboxMinecart;
        });
      }
    }
  }

  @Nullable private static ResourceLocation andromeda$getEntityId(SpawnerBlockEntity mobSpawnerBlockEntity) {
    var entry = mobSpawnerBlockEntity.getSpawner().nextSpawnData;
    if (entry == null) return BuiltInRegistries.ENTITY_TYPE.getDefaultKey();
    String identifier = entry.entityToSpawn().getString("id").orElse("");

    try {
      return StringUtils.isEmpty(identifier)
          ? BuiltInRegistries.ENTITY_TYPE.getDefaultKey()
          : ResourceLocation.tryParse(identifier);
    } catch (ResourceLocationException e) {
      BlockPos blockPos = mobSpawnerBlockEntity.getBlockPos();
      ModuleManager.get()
          .get(Minecarts.class)
          .orElseThrow()
          .logger()
          .error(
              "Invalid entity id '{}' at spawner {}:[{},{},{}]",
              identifier,
              Objects.requireNonNull(mobSpawnerBlockEntity.getLevel())
                  .dimension()
                  .location(),
              blockPos.getX(),
              blockPos.getY(),
              blockPos.getZ());
      return BuiltInRegistries.ENTITY_TYPE.getDefaultKey();
    }
  }
}
