package dev.zenfyr.andromeda.modules.items.infinite_totem;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.andromeda.modules.items.infinite_totem.packets.NotifyClientPayload;
import dev.zenfyr.andromeda.modules.items.infinite_totem.packets.UsedCustomTotemPayload;
import dev.zenfyr.andromeda.modules.misc.creative_mode_tab.AndromedaCreativeTab;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.DeathProtection;

public final class InfiniteTotemMain {

  public static final ResourceKey<Item> INFINITE_TOTEM_KEY =
      Andromeda.key(Registries.ITEM, "infinite_totem");
  public static final Keeper<Item> INFINITE_TOTEM = Keeper.create();
  public static final Keeper<SimpleParticleType> KNOCKOFF_TOTEM_PARTICLE = Keeper.create();

  public static final Identifier USED_CUSTOM_TOTEM = Andromeda.id("used_custom_totem");
  public static final Identifier NOTIFY_CLIENT = Andromeda.id("notify_client_about_stuff_please");

  static void init() {
    var module = ModuleManager.get().get(InfiniteTotem.class).orElseThrow();

    INFINITE_TOTEM.init(Registry.register(
        BuiltInRegistries.ITEM,
        INFINITE_TOTEM_KEY,
        new Item(new Item.Properties()
            .setId(INFINITE_TOTEM_KEY)
            .stacksTo(1)
            .rarity(Rarity.EPIC)
            .component(DataComponents.DEATH_PROTECTION, DeathProtection.TOTEM_OF_UNDYING))));

    KNOCKOFF_TOTEM_PARTICLE.init(Registry.register(
        BuiltInRegistries.PARTICLE_TYPE,
        id("knockoff_totem_particles"),
        FabricParticleTypes.simple()));

    PayloadTypeRegistry.playS2C().register(UsedCustomTotemPayload.ID, UsedCustomTotemPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(NotifyClientPayload.ID, NotifyClientPayload.CODEC);

    ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT)
        .register(entries -> entries.accept(INFINITE_TOTEM.orThrow()));

    if (ModuleManager.get().get("misc/creative_mode_tab").isPresent()) {
      AndromedaCreativeTab.BUS.listen(acceptor -> acceptor.keeper(module, INFINITE_TOTEM));
    }
  }
}
