package me.melontini.andromeda;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.config.ConfigHelper;
import me.melontini.andromeda.content.managers.CustomTraderManager;
import me.melontini.andromeda.content.managers.EnderDragonManager;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.util.AdvancementGeneration;
import me.melontini.andromeda.util.AndromedaReporter;
import me.melontini.andromeda.util.SharedConstants;
import me.melontini.andromeda.util.data.EggProcessingData;
import me.melontini.andromeda.util.data.PlantTemperatureData;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.minecraft.world.PersistentStateHelper;
import me.melontini.dark_matter.api.minecraft.world.interfaces.TickableState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.text.Text;
import net.minecraft.world.PersistentState;
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
                CustomTraderManager.get(world);

            if (Config.get().dragonFight.fightTweaks) if (world.getRegistryKey() == World.END)
                EnderDragonManager.get(world);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            Andromeda.get().PLANT_DATA.clear();
            Andromeda.get().EGG_DATA.clear();
            if (Config.get().tradingGoatHorn) {
                PersistentStateHelper.consumeIfLoaded(MakeSure.notNull(server.getWorld(World.OVERWORLD)), CustomTraderManager.ID,
                        (world1, s) -> CustomTraderManager.get(world1), PersistentState::markDirty);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ItemBehaviorManager.clear();
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (Config.get().tradingGoatHorn) if (world.getRegistryKey() == World.OVERWORLD) {
                PersistentStateHelper.consumeIfLoaded(world, CustomTraderManager.ID,
                        (world1, s) -> CustomTraderManager.get(world1), TickableState::tick);
            }

            if (Config.get().dragonFight.fightTweaks) if (world.getRegistryKey() == World.END) {
                PersistentStateHelper.consumeIfLoaded(world, EnderDragonManager.ID,
                        (world1, s) -> EnderDragonManager.get(world1), TickableState::tick);
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (Config.get().recipeAdvancementsGeneration.enable) {
                ConfigHelper.run(() -> {
                    AdvancementGeneration.generateRecipeAdvancements(server);
                    server.getPlayerManager().getPlayerList().forEach(entity -> server.getPlayerManager().getAdvancementTracker(entity).reload(server.getAdvancementLoader()));
                }, "autogenRecipeAdvancements.autogenRecipeAdvancements");
            }
        });

        EntrypointRunner.runEntrypoint("andromeda:post-main", ModInitializer.class, ModInitializer::onInitialize);
    }

    @Override
    public String toString() {
        return "Andromeda{version=" + SharedConstants.MOD_VERSION + "}";
    }

    public static Andromeda get() {
        return Objects.requireNonNull(INSTANCE, "Andromeda not initialized");
    }

}
