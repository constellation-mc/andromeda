package me.melontini.andromeda;

import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.networks.ServerSideNetworking;
import me.melontini.andromeda.registries.*;
import me.melontini.andromeda.util.*;
import me.melontini.andromeda.util.data.EggProcessingData;
import me.melontini.andromeda.util.data.PlantData;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Andromeda implements ModInitializer {
    public static final String MODID = "andromeda";
    public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer(Andromeda.MODID).orElseThrow().getMetadata().getVersion().getFriendlyString();
    public static final Path HIDDEN_PATH = FabricLoader.getInstance().getGameDir().resolve(".andromeda");
    public static final boolean FABRICATION_LOADED = FabricLoader.getInstance().isModLoaded("fabrication");
    public static EntityAttributeModifier LEAF_SLOWNESS;
    public static AndromedaConfig CONFIG = AutoConfig.getConfigHolder(AndromedaConfig.class).getConfig();
    public static Map<Block, PlantData> PLANT_DATA = new HashMap<>();
    public static Map<Item, EggProcessingData> EGG_DATA = new HashMap<>();
    public static DefaultParticleType KNOCKOFF_TOTEM_PARTICLE;
    public static final RegistryKey<DamageType> AGONY = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MODID, "agony"));
    public static final RegistryKey<DamageType> BRICKED = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MODID, "bricked"));
    public static final Map<PlayerEntity, AbstractMinecartEntity> LINKING_CARTS = new HashMap<>();
    public static final Map<PlayerEntity, AbstractMinecartEntity> UNLINKING_CARTS = new HashMap<>();
    public static final IntProperty WATER_LEVEL_3 = IntProperty.of("water_level", 1, 3);

    private static void updateHiddenPath() {
        Path old = FabricLoader.getInstance().getGameDir().resolve(".m_tweaks");
        if (Files.exists(old)) {
            try {
                Files.move(old, HIDDEN_PATH);
            } catch (IOException e) {
                AndromedaLog.error("Couldn't move hidden path!", e);
            }
        }
    }

    @Override
    public void onInitialize() {
        updateHiddenPath();
        BlockRegistry.register();
        ItemRegistry.register();
        EntityTypeRegistry.register();
        ServerSideNetworking.register();
        ResourceConditionRegistry.register();
        ScreenHandlerRegistry.register();

        LEAF_SLOWNESS = new EntityAttributeModifier(UUID.fromString("f72625eb-d4c4-4e1d-8e5c-1736b9bab349"), "Leaf Slowness", -0.3, EntityAttributeModifier.Operation.MULTIPLY_BASE);
        KNOCKOFF_TOTEM_PARTICLE = FabricParticleTypes.simple();

        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MODID, "knockoff_totem_particles"), KNOCKOFF_TOTEM_PARTICLE);

        ServerWorldEvents.LOAD.register((server, world) -> {
            if (CONFIG.tradingGoatHorn) if (world.getRegistryKey() == World.OVERWORLD)
                WorldUtil.getTraderManager(world);

            if (CONFIG.dragonFight.fightTweaks) if (world.getRegistryKey() == World.END)
                WorldUtil.getEnderDragonManager(world);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            Andromeda.PLANT_DATA.clear();
            Andromeda.EGG_DATA.clear();
            if (CONFIG.tradingGoatHorn) {
                ServerWorld world = server.getWorld(World.OVERWORLD);
                if (world != null) {
                    var manager = world.getPersistentStateManager();
                    if (manager.loadedStates.containsKey("andromeda_trader_statemanager"))
                        WorldUtil.getTraderManager(world).markDirty();
                }
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            Andromeda.LINKING_CARTS.clear();
            Andromeda.UNLINKING_CARTS.clear();
            ItemBehaviorManager.clear();
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (CONFIG.tradingGoatHorn) if (world.getRegistryKey() == World.OVERWORLD) {
                var manager = world.getPersistentStateManager();
                if (manager.loadedStates.containsKey("andromeda_trader_statemanager"))
                    WorldUtil.getTraderManager(world).tick();
            }

            if (CONFIG.dragonFight.fightTweaks) if (world.getRegistryKey() == World.END) {
                var manager = world.getPersistentStateManager();
                if (manager.loadedStates.containsKey("andromeda_ender_dragon_fight"))
                    WorldUtil.getEnderDragonManager(world).tick();
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (CONFIG.autogenRecipeAdvancements.autogenRecipeAdvancements) {
                MiscUtil.generateRecipeAdvancements(server);
                server.getPlayerManager().getPlayerList().forEach(entity -> server.getPlayerManager().getAdvancementTracker(entity).reload(server.getAdvancementLoader()));
            }
        });
    }
}
