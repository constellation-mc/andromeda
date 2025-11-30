package dev.zenfyr.andromeda.modules.entities.minecarts;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.andromeda.modules.entities.minecarts.entities.AnvilMinecartEntity;
import dev.zenfyr.andromeda.modules.entities.minecarts.entities.JukeboxMinecartEntity;
import dev.zenfyr.andromeda.modules.entities.minecarts.entities.NoteBlockMinecartEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
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
      ANVIL_MINECART_ENTITY.init(Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          id("anvil_minecart"),
          FabricEntityTypeBuilder.<AnvilMinecartEntity>create(
                  MobCategory.MISC, AnvilMinecartEntity::new)
              .dimensions(new EntityDimensions(0.98F, 0.7F, true))
              .build()));
    }

    if (config.isNoteBlockMinecartOn) {
      NOTEBLOCK_MINECART_ENTITY.init(Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          id("note_block_minecart"),
          FabricEntityTypeBuilder.<NoteBlockMinecartEntity>create(
                  MobCategory.MISC, NoteBlockMinecartEntity::new)
              .dimensions(new EntityDimensions(0.98F, 0.7F, true))
              .build()));
    }

    if (config.isJukeboxMinecartOn) {
      JUKEBOX_MINECART_ENTITY.init(Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          id("jukebox_minecart"),
          FabricEntityTypeBuilder.<JukeboxMinecartEntity>create(
                  MobCategory.MISC, JukeboxMinecartEntity::new)
              .dimensions(new EntityDimensions(0.98F, 0.7F, true))
              .build()));
    }
  }
}
