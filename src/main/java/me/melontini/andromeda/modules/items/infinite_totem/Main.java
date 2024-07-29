package me.melontini.andromeda.modules.items.infinite_totem;

import static me.melontini.andromeda.common.Andromeda.id;

import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.AndromedaItemGroup;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.modules.items.infinite_totem.packets.NotifyClientPayload;
import me.melontini.andromeda.modules.items.infinite_totem.packets.UsedCustomTotemPayload;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public final class Main {

  public static final Keeper<Item> INFINITE_TOTEM = Keeper.create();
  public static final Keeper<SimpleParticleType> KNOCKOFF_TOTEM_PARTICLE = Keeper.create();

  public static final Identifier USED_CUSTOM_TOTEM = Andromeda.id("used_custom_totem");
  public static final Identifier NOTIFY_CLIENT = Andromeda.id("notify_client_about_stuff_please");

  static void init(InfiniteTotem module) {
    INFINITE_TOTEM.init(RegistryUtil.register(
        Registries.ITEM,
        id("infinite_totem"),
        () -> new Item(new Item.Settings().maxCount(1).rarity(Rarity.EPIC))));

    KNOCKOFF_TOTEM_PARTICLE.init(RegistryUtil.register(
        Registries.PARTICLE_TYPE, id("knockoff_totem_particles"), FabricParticleTypes::simple));

    PayloadTypeRegistry.playS2C().register(UsedCustomTotemPayload.ID, UsedCustomTotemPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(NotifyClientPayload.ID, NotifyClientPayload.CODEC);

    AndromedaItemGroup.accept(
        acceptor -> acceptor.keeper(module, ItemGroups.COMBAT, INFINITE_TOTEM));
  }
}
