package me.melontini.andromeda.registries;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.items.LockpickItem;
import me.melontini.andromeda.items.RoseOfTheValley;
import me.melontini.andromeda.items.boats.FurnaceBoatItem;
import me.melontini.andromeda.items.boats.HopperBoatItem;
import me.melontini.andromeda.items.boats.JukeboxBoatItem;
import me.melontini.andromeda.items.boats.TNTBoatItem;
import me.melontini.andromeda.items.minecarts.AnvilMinecartItem;
import me.melontini.andromeda.items.minecarts.JukeBoxMinecartItem;
import me.melontini.andromeda.items.minecarts.NoteBlockMinecartItem;
import me.melontini.andromeda.items.minecarts.SpawnerMinecartItem;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.AndromedaTexts;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.interfaces.DarkMatterEntries;
import me.melontini.dark_matter.api.minecraft.client.util.DrawUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static me.melontini.andromeda.registries.Common.call;
import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.util.ItemStackUtil.getStackOrEmpty;
import static me.melontini.dark_matter.api.content.RegistryUtil.asItem;

public class ItemRegistry {

    private static ItemRegistry INSTANCE;

    public RoseOfTheValley ROSE_OF_THE_VALLEY = asItem(BlockRegistry.get().ROSE_OF_THE_VALLEY);

    public SpawnerMinecartItem SPAWNER_MINECART = ContentBuilder.ItemBuilder
            .create(id("spawner_minecart"), () -> new SpawnerMinecartItem(AbstractMinecartEntity.Type.SPAWNER, new FabricItemSettings().maxCount(1)))
            .itemGroup(call(() -> ItemGroup.TRANSPORTATION)).build();

    public AnvilMinecartItem ANVIL_MINECART = ContentBuilder.ItemBuilder
            .create(id("anvil_minecart"), () -> new AnvilMinecartItem(new FabricItemSettings().maxCount(1)))
            .itemGroup(call(() -> ItemGroup.TRANSPORTATION)).register(Config.get().newMinecarts.isAnvilMinecartOn).build();

    public NoteBlockMinecartItem NOTE_BLOCK_MINECART = ContentBuilder.ItemBuilder
            .create(id("note_block_minecart"), () -> new NoteBlockMinecartItem(new FabricItemSettings().maxCount(1)))
            .itemGroup(call(() -> ItemGroup.TRANSPORTATION)).register(Config.get().newMinecarts.isNoteBlockMinecartOn).build();

    public JukeBoxMinecartItem JUKEBOX_MINECART = ContentBuilder.ItemBuilder
            .create(id("jukebox_minecart"), () -> new JukeBoxMinecartItem(new FabricItemSettings().maxCount(1)))
            .itemGroup(call(() -> ItemGroup.TRANSPORTATION)).register(Config.get().newMinecarts.isJukeboxMinecartOn).build();

    public Item INFINITE_TOTEM = ContentBuilder.ItemBuilder
            .create(id("infinite_totem"), () -> new Item(new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC)))
            .itemGroup(call(() -> ItemGroup.COMBAT)).register(Config.get().totemSettings.enableInfiniteTotem).build();

    public Item LOCKPICK = ContentBuilder.ItemBuilder
            .create(id("lockpick"), () -> new LockpickItem(new FabricItemSettings().maxCount(16)))
            .itemGroup(call(() -> ItemGroup.TOOLS)).register(Config.get().lockpickEnabled).build();

    public BlockItem INCUBATOR = asItem(BlockRegistry.get().INCUBATOR_BLOCK);

    private ItemStack ITEM_GROUP_ICON;

    public ItemGroup GROUP = call(() -> ContentBuilder.ItemGroupBuilder.create(id("group"))
            .entries(entries -> {
                List<ItemStack> misc = new ArrayList<>();
                misc.add(getStackOrEmpty(this.INCUBATOR));
                misc.add(getStackOrEmpty(this.INFINITE_TOTEM));
                misc.add(getStackOrEmpty(this.LOCKPICK));
                appendStacks(entries, misc, true);

                List<ItemStack> carts = new ArrayList<>();
                carts.add(getStackOrEmpty(this.ANVIL_MINECART));
                carts.add(getStackOrEmpty(this.JUKEBOX_MINECART));
                carts.add(getStackOrEmpty(this.NOTE_BLOCK_MINECART));
                carts.add(getStackOrEmpty(this.SPAWNER_MINECART));
                appendStacks(entries, carts, true);

                List<ItemStack> boats = new ArrayList<>();
                for (BoatEntity.Type value : BoatEntity.Type.values()) {
                    boats.add(call(() -> getStackOrEmpty(Registry.ITEM.get(boatId(value, "furnace")))));
                    boats.add(call(() -> getStackOrEmpty(Registry.ITEM.get(boatId(value, "hopper")))));
                    boats.add(call(() -> getStackOrEmpty(Registry.ITEM.get(boatId(value, "tnt")))));
                    boats.add(call(() -> getStackOrEmpty(Registry.ITEM.get(boatId(value, "jukebox")))));
                }
                appendStacks(entries, boats, false);
            }).icon(this::getAndSetIcon).animatedIcon(() -> (group, matrixStack, itemX, itemY, selected, isTopRow) -> call(() -> {
                MinecraftClient client = MinecraftClient.getInstance();

                float angle = Util.getMeasuringTimeMs() * 0.09f;
                matrixStack.push();
                matrixStack.translate(itemX, itemY, 100.0F + client.getItemRenderer().zOffset);
                matrixStack.translate(8.0, 8.0, 0.0);
                matrixStack.scale(1.0F, -1.0F, 1.0F);
                matrixStack.scale(16.0F, 16.0F, 16.0F);
                matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(angle));
                BakedModel model = client.getItemRenderer().getModel(getAndSetIcon(), null, null, 0);
                DrawUtil.renderGuiItemModelCustomMatrixNoTransform(matrixStack, getAndSetIcon(), model);
                matrixStack.pop();
            })).displayName(AndromedaTexts.ITEM_GROUP_NAME).build());

    public static ItemRegistry get() {
        return Objects.requireNonNull(INSTANCE, "%s requested too early!".formatted(INSTANCE.getClass()));
    }

    public static void register() {
        if (INSTANCE != null) throw new IllegalStateException("%s already initialized!".formatted(INSTANCE.getClass()));

        INSTANCE = new ItemRegistry();
        for (BoatEntity.Type value : BoatEntity.Type.values()) {
            ContentBuilder.ItemBuilder.create(boatId(value, "furnace"), () -> new FurnaceBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(call(() -> ItemGroup.TRANSPORTATION)).register(Config.get().newBoats.isFurnaceBoatOn).build();
            ContentBuilder.ItemBuilder.create(boatId(value, "jukebox"), () -> new JukeboxBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(call(() -> ItemGroup.TRANSPORTATION)).register(Config.get().newBoats.isJukeboxBoatOn).build();
            ContentBuilder.ItemBuilder.create(boatId(value, "tnt"), () -> new TNTBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(call(() -> ItemGroup.TRANSPORTATION)).register(Config.get().newBoats.isTNTBoatOn).build();
            ContentBuilder.ItemBuilder.create(boatId(value, "hopper"), () -> new HopperBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(call(() -> ItemGroup.TRANSPORTATION)).register(Config.get().newBoats.isHopperBoatOn).build();
        }
        AndromedaLog.info("%s init complete!".formatted(INSTANCE.getClass().getSimpleName()));
    }

    public static Identifier boatId(BoatEntity.Type type, String boat) {
        return id(type.getName().replace(":", "_") + "_boat_with_" + boat);
    }

    private static void appendStacks(DarkMatterEntries entries, Collection<ItemStack> list, boolean lineBreak) {
        if (list == null || list.isEmpty()) return; //we shouldn't add line breaks if there are no items.
        list.removeIf(stack -> stack == null || stack.isEmpty());
        if (list.isEmpty()) return;

        int rows = MathStuff.fastCeil(list.size() / 9d);
        entries.addAll(list, DarkMatterEntries.Visibility.TAB);
        int left = (rows * 9) - list.size();
        for (int i = 0; i < left; i++) {
            entries.add(ItemStack.EMPTY, DarkMatterEntries.Visibility.TAB); //fill the gaps
        }
        if (lineBreak) entries.addAll(DefaultedList.ofSize(9, ItemStack.EMPTY), DarkMatterEntries.Visibility.TAB); //line break
    }

    private ItemStack getAndSetIcon() {
        if (ITEM_GROUP_ICON == null) {
            if (Config.get().unknown && ROSE_OF_THE_VALLEY != null) {
                ITEM_GROUP_ICON = new ItemStack(ROSE_OF_THE_VALLEY);
            } else if (Config.get().totemSettings.enableInfiniteTotem && INFINITE_TOTEM != null) {
                ITEM_GROUP_ICON = new ItemStack(INFINITE_TOTEM);
            } else if (Config.get().incubatorSettings.enableIncubator && INCUBATOR != null) {
                ITEM_GROUP_ICON = new ItemStack(INCUBATOR);
            } else ITEM_GROUP_ICON = new ItemStack(Items.BEDROCK);
        }

        return ITEM_GROUP_ICON;
    }
}
