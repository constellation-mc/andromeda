package me.melontini.andromeda;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.config.ConfigHelper;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.util.AdvancementGeneration;
import me.melontini.andromeda.util.AndromedaReporter;
import me.melontini.andromeda.util.WorldUtil;
import me.melontini.andromeda.util.data.EggProcessingData;
import me.melontini.andromeda.util.data.PlantTemperatureData;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Andromeda {

    private static Andromeda INSTANCE;

    public Map<Block, PlantTemperatureData> PLANT_DATA = new HashMap<>();
    public Map<Item, EggProcessingData> EGG_DATA = new HashMap<>();

    public DefaultParticleType KNOCKOFF_TOTEM_PARTICLE;

    public EntityAttributeModifier LEAF_SLOWNESS;

    public RegistryKey<DamageType> AGONY;
    public RegistryKey<DamageType> BRICKED;

    public static void init() {
        INSTANCE = new Andromeda();
        INSTANCE.onInitialize();
    }

    private void onInitialize() {
        EntrypointRunner.runEntrypoint("andromeda:pre-main", ModInitializer.class, ModInitializer::onInitialize);

        AndromedaReporter.init();
        Common.bootstrap();

        ServerWorldEvents.LOAD.register((server, world) -> {
            if (Config.get().tradingGoatHorn) if (world.getRegistryKey() == World.OVERWORLD)
                WorldUtil.getTraderManager(world);

            if (Config.get().dragonFight.fightTweaks) if (world.getRegistryKey() == World.END)
                WorldUtil.getEnderDragonManager(world);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            Andromeda.get().PLANT_DATA.clear();
            Andromeda.get().EGG_DATA.clear();
            if (Config.get().tradingGoatHorn) {
                ServerWorld world = server.getWorld(World.OVERWORLD);
                if (world != null) {
                    var manager = world.getPersistentStateManager();
                    if (manager.loadedStates.containsKey("andromeda_trader_statemanager"))
                        WorldUtil.getTraderManager(world).markDirty();
                }
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ItemBehaviorManager.clear();
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (Config.get().tradingGoatHorn) if (world.getRegistryKey() == World.OVERWORLD) {
                var manager = world.getPersistentStateManager();
                if (manager.loadedStates.containsKey("andromeda_trader_statemanager"))
                    WorldUtil.getTraderManager(world).tick();
            }

            if (Config.get().dragonFight.fightTweaks) if (world.getRegistryKey() == World.END) {
                var manager = world.getPersistentStateManager();
                if (manager.loadedStates.containsKey("andromeda_ender_dragon_fight"))
                    WorldUtil.getEnderDragonManager(world).tick();
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (Config.get().recipeAdvancementsGeneration.enable) {
                ConfigHelper.run(() -> AdvancementGeneration.generateRecipeAdvancements(server), "autogenRecipeAdvancements.autogenRecipeAdvancements");
                server.getPlayerManager().getPlayerList().forEach(entity -> server.getPlayerManager().getAdvancementTracker(entity).reload(server.getAdvancementLoader()));
            }
        });

        EntrypointRunner.runEntrypoint("andromeda:post-main", ModInitializer.class, ModInitializer::onInitialize);
    }

    public static Andromeda get() {
        return Objects.requireNonNull(INSTANCE, "Andromeda not initialized");
    }

}
