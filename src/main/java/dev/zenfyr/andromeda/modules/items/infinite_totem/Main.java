package dev.zenfyr.andromeda.modules.items.infinite_totem;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.AndromedaItemGroup;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.pulsar.registry.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class Main {

  public static final Keeper<Item> INFINITE_TOTEM = Keeper.create();
  public static final Keeper<SimpleParticleType> KNOCKOFF_TOTEM_PARTICLE = Keeper.create();

  public static final ResourceLocation USED_CUSTOM_TOTEM = Andromeda.id("used_custom_totem");
  public static final ResourceLocation NOTIFY_CLIENT =
      Andromeda.id("notify_client_about_stuff_please");

  static void init() {
    var module = ModuleManager.get().get(InfiniteTotem.class).orElseThrow();

    INFINITE_TOTEM.init(RegistryUtil.register(
        BuiltInRegistries.ITEM,
        id("infinite_totem"),
        () -> new Item(new FabricItemSettings().stacksTo(1).rarity(Rarity.EPIC))));

    KNOCKOFF_TOTEM_PARTICLE.init(RegistryUtil.register(
        BuiltInRegistries.PARTICLE_TYPE,
        id("knockoff_totem_particles"),
        FabricParticleTypes::simple));

    AndromedaItemGroup.BUS.listen(
        acceptor -> acceptor.keeper(module, CreativeModeTabs.COMBAT, INFINITE_TOTEM));
  }
}
