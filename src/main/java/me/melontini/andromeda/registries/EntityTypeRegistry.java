package me.melontini.andromeda.registries;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.entity.FlyingItemEntity;
import me.melontini.andromeda.entity.vehicle.boats.FurnaceBoatEntity;
import me.melontini.andromeda.entity.vehicle.boats.HopperBoatEntity;
import me.melontini.andromeda.entity.vehicle.boats.JukeboxBoatEntity;
import me.melontini.andromeda.entity.vehicle.boats.TNTBoatEntity;
import me.melontini.andromeda.entity.vehicle.minecarts.AnvilMinecartEntity;
import me.melontini.andromeda.entity.vehicle.minecarts.JukeboxMinecartEntity;
import me.melontini.andromeda.entity.vehicle.minecarts.NoteBlockMinecartEntity;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;

import static me.melontini.andromeda.util.SharedConstants.MODID;

public class EntityTypeRegistry {
    public static EntityType<AnvilMinecartEntity> ANVIL_MINECART_ENTITY = RegistryUtil.createEntityType(Andromeda.CONFIG.newMinecarts.isAnvilMinecartOn, new Identifier(MODID, "anvil_minecart"),
            FabricEntityTypeBuilder.<AnvilMinecartEntity>create(SpawnGroup.MISC, AnvilMinecartEntity::new).dimensions(new EntityDimensions(0.98F, 0.7F, true)));

    public static EntityType<NoteBlockMinecartEntity> NOTEBLOCK_MINECART_ENTITY = RegistryUtil.createEntityType(Andromeda.CONFIG.newMinecarts.isNoteBlockMinecartOn, new Identifier(MODID, "note_block_minecart"),
            FabricEntityTypeBuilder.<NoteBlockMinecartEntity>create(SpawnGroup.MISC, NoteBlockMinecartEntity::new).dimensions(new EntityDimensions(0.98F, 0.7F, true)));

    public static EntityType<JukeboxMinecartEntity> JUKEBOX_MINECART_ENTITY = RegistryUtil.createEntityType(Andromeda.CONFIG.newMinecarts.isJukeboxMinecartOn, new Identifier(MODID, "jukebox_minecart"),
            FabricEntityTypeBuilder.<JukeboxMinecartEntity>create(SpawnGroup.MISC, JukeboxMinecartEntity::new).dimensions(new EntityDimensions(0.98F, 0.7F, true)));

    public static EntityType<TNTBoatEntity> BOAT_WITH_TNT = RegistryUtil.createEntityType(Andromeda.CONFIG.newBoats.isTNTBoatOn, new Identifier(MODID, "tnt_boat"),
            FabricEntityTypeBuilder.<TNTBoatEntity>create(SpawnGroup.MISC, TNTBoatEntity::new).dimensions(new EntityDimensions(1.375F, 0.5625F, true)));

    public static EntityType<FurnaceBoatEntity> BOAT_WITH_FURNACE = RegistryUtil.createEntityType(Andromeda.CONFIG.newBoats.isFurnaceBoatOn, new Identifier(MODID, "furnace_boat"),
            FabricEntityTypeBuilder.<FurnaceBoatEntity>create(SpawnGroup.MISC, FurnaceBoatEntity::new).dimensions(new EntityDimensions(1.375F, 0.5625F, true)));

    public static EntityType<JukeboxBoatEntity> BOAT_WITH_JUKEBOX = RegistryUtil.createEntityType(Andromeda.CONFIG.newBoats.isJukeboxBoatOn, new Identifier(MODID, "jukebox_boat"),
            FabricEntityTypeBuilder.<JukeboxBoatEntity>create(SpawnGroup.MISC, JukeboxBoatEntity::new).dimensions(new EntityDimensions(1.375F, 0.5625F, true)));

    public static EntityType<HopperBoatEntity> BOAT_WITH_HOPPER = RegistryUtil.createEntityType(Andromeda.CONFIG.newBoats.isHopperBoatOn, new Identifier(MODID, "hopper_boat"),
            FabricEntityTypeBuilder.<HopperBoatEntity>create(SpawnGroup.MISC, HopperBoatEntity::new).dimensions(new EntityDimensions(1.375F, 0.5625F, true)));

    public static EntityType<FlyingItemEntity> FLYING_ITEM = RegistryUtil.createEntityType(new Identifier(MODID, "flying_item"),
            FabricEntityTypeBuilder.<FlyingItemEntity>create(SpawnGroup.MISC, FlyingItemEntity::new).dimensions(new EntityDimensions(0.25F, 0.25F, true))
                    .trackRangeChunks(4).trackedUpdateRate(10));

    public static void register() {
        AndromedaLog.info("EntityTypeRegistry init complete!");
    }


}
