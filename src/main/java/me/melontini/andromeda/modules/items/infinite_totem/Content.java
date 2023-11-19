package me.melontini.andromeda.modules.items.infinite_totem;

import me.melontini.andromeda.registries.Keeper;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Rarity;

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.registries.Common.start;

public class Content {

    @Feature("totemSettings.enableInfiniteTotem")
    public static final Keeper<Item> INFINITE_TOTEM = start(() -> ContentBuilder.ItemBuilder
            .create(id("infinite_totem"), () -> new Item(new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC)))
            .itemGroup(ItemGroup.COMBAT));

    @Feature("totemSettings.enableInfiniteTotem")
    public static Keeper<DefaultParticleType> KNOCKOFF_TOTEM_PARTICLE = Keeper.of(() -> () ->
            RegistryUtil.create(id("knockoff_totem_particles"), "particle_type", FabricParticleTypes::simple));
}
