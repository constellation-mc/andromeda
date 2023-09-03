package me.melontini.andromeda;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.config.ConfigHelper;
import me.melontini.andromeda.content.commands.DamageCommand;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.networks.ServerSideNetworking;
import me.melontini.andromeda.registries.*;
import me.melontini.andromeda.util.AdvancementGeneration;
import me.melontini.andromeda.util.AndromedaReporter;
import me.melontini.andromeda.util.SharedConstants;
import me.melontini.andromeda.util.WorldUtil;
import me.melontini.andromeda.util.data.EggProcessingData;
import me.melontini.andromeda.util.data.PlantTemperatureData;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Andromeda {

    private static Andromeda INSTANCE;

    public Map<Block, PlantTemperatureData> PLANT_DATA = new HashMap<>();
    public Map<Item, EggProcessingData> EGG_DATA = new HashMap<>();

    public DefaultParticleType KNOCKOFF_TOTEM_PARTICLE;

    public EntityAttributeModifier LEAF_SLOWNESS;

    public final DamageSource AGONY = new DamageSource("andromeda_agony");

    public static DamageSource bricked(@Nullable Entity attacker) {
        return new BrickedDamageSource(attacker);
    }

    public static void init() {
        INSTANCE = new Andromeda();
        INSTANCE.onInitialize();
    }

    private void onInitialize() {
        EntrypointRunner.runEntrypoint("andromeda:pre-main", ModInitializer.class, ModInitializer::onInitialize);

        AndromedaReporter.registerCrashHandler();
        BlockRegistry.register();
        ItemRegistry.register();
        EntityTypeRegistry.register();
        ServerSideNetworking.register();
        ResourceRegistry.register();
        ScreenHandlerRegistry.register();
        TagRegistry.register();

        LEAF_SLOWNESS = new EntityAttributeModifier(UUID.fromString("f72625eb-d4c4-4e1d-8e5c-1736b9bab349"), "Leaf Slowness", -0.3, EntityAttributeModifier.Operation.MULTIPLY_BASE);
        KNOCKOFF_TOTEM_PARTICLE = FabricParticleTypes.simple();

        Registry.register(Registries.PARTICLE_TYPE, new Identifier(SharedConstants.MODID, "knockoff_totem_particles"), KNOCKOFF_TOTEM_PARTICLE);

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
            if (Config.get().autogenRecipeAdvancements.autogenRecipeAdvancements) {
                AdvancementGeneration.generateRecipeAdvancements(server);
                server.getPlayerManager().getPlayerList().forEach(entity -> server.getPlayerManager().getAdvancementTracker(entity).reload(server.getAdvancementLoader()));
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (Config.get().damageBackport) DamageCommand.register(dispatcher);
        });

        ConfigHelper.writeConfigToFile(true);

        EntrypointRunner.runEntrypoint("andromeda:post-main", ModInitializer.class, ModInitializer::onInitialize);
    }

    public static Andromeda get() {
        return Objects.requireNonNull(INSTANCE, "Andromeda not initialized");
    }

    private static class BrickedDamageSource extends DamageSource {
        private final Entity attacker;

        public BrickedDamageSource(Entity attacker) {
            super("andromeda_bricked");
            this.attacker = attacker;
        }

        @Override
        public Text getDeathMessage(LivingEntity entity) {
            return TextUtil.translatable("death.attack.andromeda_bricked", entity.getDisplayName(), attacker != null ? attacker.getDisplayName() : "");
        }

        @Nullable
        @Override
        public Entity getAttacker() {
            return attacker;
        }
    }

}
