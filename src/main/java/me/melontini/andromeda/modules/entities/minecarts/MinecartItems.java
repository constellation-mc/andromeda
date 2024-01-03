package me.melontini.andromeda.modules.entities.minecarts;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.conflicts.CommonItemGroups;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.andromeda.modules.entities.minecarts.items.AnvilMinecartItem;
import me.melontini.andromeda.modules.entities.minecarts.items.JukeBoxMinecartItem;
import me.melontini.andromeda.modules.entities.minecarts.items.NoteBlockMinecartItem;
import me.melontini.andromeda.modules.entities.minecarts.items.SpawnerMinecartItem;
import me.melontini.andromeda.modules.items.minecart_block_picking.MinecartBlockPicking;
import me.melontini.andromeda.modules.items.minecart_block_picking.PickUpBehaviorHandler;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.minecraft.data.NbtBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;

import static me.melontini.andromeda.common.registries.Common.id;
import static me.melontini.andromeda.common.registries.Common.start;

public class MinecartItems {

    private static Minecarts MODULE;

    public static final Keeper<SpawnerMinecartItem> SPAWNER_MINECART = start(() -> ContentBuilder.ItemBuilder
            .create(id("spawner_minecart"), () -> new SpawnerMinecartItem(new FabricItemSettings().maxCount(1)))
            .itemGroup(CommonItemGroups.transport())
            .register(() -> MODULE.config().isSpawnerMinecartOn))
            .afterInit(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(MODULE, item)));

    public static final Keeper<AnvilMinecartItem> ANVIL_MINECART = start(() -> ContentBuilder.ItemBuilder
            .create(id("anvil_minecart"), () -> new AnvilMinecartItem(new FabricItemSettings().maxCount(1)))
            .itemGroup(CommonItemGroups.transport())
            .register(() -> MODULE.config().isAnvilMinecartOn))
            .afterInit(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(MODULE, item)));

    public static final Keeper<NoteBlockMinecartItem> NOTE_BLOCK_MINECART = start(() -> ContentBuilder.ItemBuilder
            .create(id("note_block_minecart"), () -> new NoteBlockMinecartItem(new FabricItemSettings().maxCount(1)))
            .itemGroup(CommonItemGroups.transport())
            .register(() -> MODULE.config().isNoteBlockMinecartOn))
            .afterInit(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(MODULE, item)));

    public static final Keeper<JukeBoxMinecartItem> JUKEBOX_MINECART = start(() -> ContentBuilder.ItemBuilder
            .create(id("jukebox_minecart"), () -> new JukeBoxMinecartItem(new FabricItemSettings().maxCount(1)))
            .itemGroup(CommonItemGroups.transport())
            .register(() -> MODULE.config().isJukeboxMinecartOn))
            .afterInit(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(MODULE, item)));

    public static void init() {
        ModuleManager.get().getModule(MinecartBlockPicking.class).ifPresent(m -> {
            SPAWNER_MINECART.ifPresent(item -> PickUpBehaviorHandler.registerPickUpBehavior(Blocks.SPAWNER, (state, world, pos) -> {
                if (m.config().spawnerPicking) {
                    MobSpawnerBlockEntity mobSpawnerBlockEntity = (MobSpawnerBlockEntity) MakeSure.notNull(world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
                    ItemStack spawnerMinecart = new ItemStack(item, 1);
                    spawnerMinecart.setNbt(NbtBuilder.create().putString("Entity", String.valueOf(andromeda$getEntityId(mobSpawnerBlockEntity))).build());
                    return spawnerMinecart;
                }
                return null;
            }));

            ANVIL_MINECART.ifPresent(item -> PickUpBehaviorHandler.registerPickUpBehavior(Blocks.ANVIL, (state, world, pos) -> new ItemStack(item)));

            NOTE_BLOCK_MINECART.ifPresent(item -> PickUpBehaviorHandler.registerPickUpBehavior(Blocks.NOTE_BLOCK, (state, world, pos) -> {
                NoteBlock noteBlock = (NoteBlock) state.getBlock();
                int noteProp = noteBlock.getStateWithProperties(state).get(Properties.NOTE);
                ItemStack noteBlockMinecart = new ItemStack(item);

                noteBlockMinecart.setNbt(NbtBuilder.create().putInt("Note", noteProp).build());
                return noteBlockMinecart;
            }));

            JUKEBOX_MINECART.ifPresent(item -> PickUpBehaviorHandler.registerPickUpBehavior(Blocks.JUKEBOX, (state, world, pos) -> {
                JukeboxBlockEntity jukeboxBlockEntity = (JukeboxBlockEntity) MakeSure.notNull(world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));

                ItemStack record = jukeboxBlockEntity.getRecord();
                ItemStack jukeboxMinecart = new ItemStack(item);

                if (!record.isEmpty()) {
                    world.syncWorldEvent(WorldEvents.MUSIC_DISC_PLAYED, pos, 0);
                    jukeboxMinecart.setNbt(NbtBuilder.create().put("Items", record.writeNbt(new NbtCompound())).build());
                }
                jukeboxBlockEntity.clear();
                return jukeboxMinecart;
            }));
        });
    }

    @Nullable
    @Unique
    private static Identifier andromeda$getEntityId(MobSpawnerBlockEntity mobSpawnerBlockEntity) {
        String identifier = mobSpawnerBlockEntity.getLogic().spawnEntry.getNbt().getString("id");

        try {
            return StringUtils.isEmpty(identifier) ? CommonRegistries.entityTypes().getDefaultId() : new Identifier(identifier);
        } catch (InvalidIdentifierException e) {
            BlockPos blockPos = mobSpawnerBlockEntity.getPos();
            AndromedaLog.error(String.format("Invalid entity id '%s' at spawner %s:[%s,%s,%s]", identifier, Objects.requireNonNull(mobSpawnerBlockEntity.getWorld()).getRegistryKey().getValue(), blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            return CommonRegistries.entityTypes().getDefaultId();
        }
    }
}
