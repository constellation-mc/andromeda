package me.melontini.andromeda.registries;

import me.melontini.andromeda.entity.FlyingItemEntity;
import me.melontini.andromeda.entity.vehicle.boats.FurnaceBoatEntity;
import me.melontini.andromeda.entity.vehicle.boats.HopperBoatEntity;
import me.melontini.andromeda.entity.vehicle.boats.JukeboxBoatEntity;
import me.melontini.andromeda.entity.vehicle.boats.TNTBoatEntity;
import me.melontini.andromeda.entity.vehicle.minecarts.AnvilMinecartEntity;
import me.melontini.andromeda.entity.vehicle.minecarts.JukeboxMinecartEntity;
import me.melontini.andromeda.entity.vehicle.minecarts.NoteBlockMinecartEntity;
import me.melontini.andromeda.util.annotations.AndromedaRegistry;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

import static me.melontini.andromeda.registries.Common.id;

@AndromedaRegistry("andromeda:entity_type_registry")
public class EntityTypeRegistry {

    @Feature("newMinecarts.isAnvilMinecartOn")
    public static final Keeper<EntityType<AnvilMinecartEntity>> ANVIL_MINECART_ENTITY = Keeper.of(() -> () ->
            RegistryUtil.createEntityType(id("anvil_minecart"),
                    FabricEntityTypeBuilder.<AnvilMinecartEntity>create(SpawnGroup.MISC, AnvilMinecartEntity::new)
                            .dimensions(new EntityDimensions(0.98F, 0.7F, true))));

    @Feature("newMinecarts.isNoteBlockMinecartOn")
    public static final Keeper<EntityType<NoteBlockMinecartEntity>> NOTEBLOCK_MINECART_ENTITY = Keeper.of(() -> () ->
            RegistryUtil.createEntityType(id("note_block_minecart"),
                    FabricEntityTypeBuilder.<NoteBlockMinecartEntity>create(SpawnGroup.MISC, NoteBlockMinecartEntity::new)
                            .dimensions(new EntityDimensions(0.98F, 0.7F, true))));

    @Feature("newMinecarts.isJukeboxMinecartOn")
    public static final Keeper<EntityType<JukeboxMinecartEntity>> JUKEBOX_MINECART_ENTITY = Keeper.of(() -> () ->
            RegistryUtil.createEntityType(id("jukebox_minecart"),
                    FabricEntityTypeBuilder.<JukeboxMinecartEntity>create(SpawnGroup.MISC, JukeboxMinecartEntity::new)
                            .dimensions(new EntityDimensions(0.98F, 0.7F, true))));

    @Feature("newBoats.isTNTBoatOn")
    public static final Keeper<EntityType<TNTBoatEntity>> BOAT_WITH_TNT = Keeper.of(() -> () ->
            RegistryUtil.createEntityType(id("tnt_boat"),
                    FabricEntityTypeBuilder.<TNTBoatEntity>create(SpawnGroup.MISC, TNTBoatEntity::new)
                            .dimensions(new EntityDimensions(1.375F, 0.5625F, true))));

    @Feature("newBoats.isFurnaceBoatOn")
    public static final Keeper<EntityType<FurnaceBoatEntity>> BOAT_WITH_FURNACE = Keeper.of(() -> () ->
            RegistryUtil.createEntityType(id("furnace_boat"),
                    FabricEntityTypeBuilder.<FurnaceBoatEntity>create(SpawnGroup.MISC, FurnaceBoatEntity::new)
                            .dimensions(new EntityDimensions(1.375F, 0.5625F, true))));

    @Feature("newBoats.isJukeboxBoatOn")
    public static final Keeper<EntityType<JukeboxBoatEntity>> BOAT_WITH_JUKEBOX = Keeper.of(() -> () ->
            RegistryUtil.createEntityType(id("jukebox_boat"),
                    FabricEntityTypeBuilder.<JukeboxBoatEntity>create(SpawnGroup.MISC, JukeboxBoatEntity::new)
                            .dimensions(new EntityDimensions(1.375F, 0.5625F, true))));

    @Feature("newBoats.isHopperBoatOn")
    public static final Keeper<EntityType<HopperBoatEntity>> BOAT_WITH_HOPPER = Keeper.of(() -> () ->
            RegistryUtil.createEntityType(id("hopper_boat"),
                    FabricEntityTypeBuilder.<HopperBoatEntity>create(SpawnGroup.MISC, HopperBoatEntity::new)
                            .dimensions(new EntityDimensions(1.375F, 0.5625F, true))));

    @Feature("throwableItems.enable")
    public static final Keeper<EntityType<FlyingItemEntity>> FLYING_ITEM = Keeper.of(() -> () ->
            RegistryUtil.createEntityType(id("flying_item"),
                    FabricEntityTypeBuilder.<FlyingItemEntity>create(SpawnGroup.MISC, FlyingItemEntity::new)
                            .dimensions(new EntityDimensions(0.25F, 0.25F, true)).trackRangeChunks(4).trackedUpdateRate(10)));

}
