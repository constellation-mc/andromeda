package me.melontini.andromeda.modules.items.infinite_totem;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Rarity;

import static me.melontini.andromeda.common.registries.Common.id;
import static me.melontini.andromeda.common.registries.Common.start;

public class Content {

    public static final Keeper<Item> INFINITE_TOTEM = start(() -> ContentBuilder.ItemBuilder
            .create(id("infinite_totem"), () -> new Item(new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC)))
            .itemGroup(Registries.ITEM_GROUP.get(ItemGroups.COMBAT))
            .register(() -> ModuleManager.get().isPresent(InfiniteTotem.class)))
            .afterInit(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(ModuleManager.quick(InfiniteTotem.class), item)));

    public static Keeper<DefaultParticleType> KNOCKOFF_TOTEM_PARTICLE = Keeper.of(() -> () ->
            RegistryUtil.create(() -> ModuleManager.get().isPresent(InfiniteTotem.class),
                    id("knockoff_totem_particles"), "particle_type", FabricParticleTypes::simple));
}
