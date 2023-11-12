package me.melontini.andromeda.modules.entities.minecarts;

import me.melontini.andromeda.modules.entities.minecarts.entities.AnvilMinecartEntity;
import me.melontini.andromeda.modules.entities.minecarts.entities.JukeboxMinecartEntity;
import me.melontini.andromeda.modules.entities.minecarts.entities.NoteBlockMinecartEntity;
import me.melontini.andromeda.registries.Keeper;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

import static me.melontini.andromeda.registries.Common.id;

public class MinecartEntities {

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

}
