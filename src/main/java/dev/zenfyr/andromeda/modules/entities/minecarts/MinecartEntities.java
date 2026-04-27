package dev.zenfyr.andromeda.modules.entities.minecarts;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.andromeda.modules.entities.boats.packets.SoundPayloadHolder;
import dev.zenfyr.andromeda.modules.entities.minecarts.entities.AnvilMinecartEntity;
import dev.zenfyr.andromeda.modules.entities.minecarts.entities.JukeboxMinecartEntity;
import dev.zenfyr.andromeda.modules.entities.minecarts.entities.NoteBlockMinecartEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class MinecartEntities {

  public static final Keeper<EntityType<AnvilMinecartEntity>> ANVIL_MINECART_ENTITY =
      Keeper.create();
  public static final Keeper<EntityType<NoteBlockMinecartEntity>> NOTEBLOCK_MINECART_ENTITY =
      Keeper.create();
  public static final Keeper<EntityType<JukeboxMinecartEntity>> JUKEBOX_MINECART_ENTITY =
      Keeper.create();

  static void init() {
    var config = Andromeda.MAIN.get(Minecarts.MAIN_CONFIG);

    if (config.isAnvilMinecartOn) {
      var key = ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), id("anvil_minecart"));
      ANVIL_MINECART_ENTITY.init(Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          key,
          EntityType.Builder.<AnvilMinecartEntity>of(AnvilMinecartEntity::new, MobCategory.MISC)
              .sized(0.98F, 0.7F)
              .build(key)));
    }

    if (config.isNoteBlockMinecartOn) {
      var key = ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), id("note_block_minecart"));
      NOTEBLOCK_MINECART_ENTITY.init(Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          key,
          EntityType.Builder.<NoteBlockMinecartEntity>of(
                  NoteBlockMinecartEntity::new, MobCategory.MISC)
              .sized(0.98F, 0.7F)
              .build(key)));
    }

    if (config.isJukeboxMinecartOn) {
      SoundPayloadHolder.init();

      var key = ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), id("jukebox_minecart"));
      JUKEBOX_MINECART_ENTITY.init(Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          key,
          EntityType.Builder.<JukeboxMinecartEntity>of(JukeboxMinecartEntity::new, MobCategory.MISC)
              .sized(0.98F, 0.7F)
              .build(key)));
    }
  }
}
