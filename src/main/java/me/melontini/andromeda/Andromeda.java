package me.melontini.andromeda;

import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.config.AndromedaFeatureManager;
import me.melontini.andromeda.content.commands.DamageCommand;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.networks.ServerSideNetworking;
import me.melontini.andromeda.registries.*;
import me.melontini.andromeda.util.*;
import me.melontini.andromeda.util.data.EggProcessingData;
import me.melontini.andromeda.util.data.PlantData;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Andromeda implements ModInitializer {
    public static EntityAttributeModifier LEAF_SLOWNESS;
    public static AndromedaConfig CONFIG = Utilities.supply(() -> {
        AutoConfig.register(AndromedaConfig.class, GsonConfigSerializer::new);

        AutoConfig.getConfigHolder(AndromedaConfig.class).registerSaveListener((configHolder, config) -> {
            AndromedaFeatureManager.processFeatures(config);
            return ActionResult.SUCCESS;
        });

        return AutoConfig.getConfigHolder(AndromedaConfig.class).getConfig();
    });
    public static Map<Block, PlantData> PLANT_DATA = new HashMap<>();
    public static Map<Item, EggProcessingData> EGG_DATA = new HashMap<>();
    public static DefaultParticleType KNOCKOFF_TOTEM_PARTICLE;
    public static final DamageSource AGONY = new DamageSource("andromeda_agony");

    public static DamageSource bricked(@Nullable Entity attacker) {
        return new BrickedDamageSource(attacker);
    }

    @Override
    public void onInitialize() {
        try {
            initMain();
        } catch (Exception e) {
            String cause = CauseFinder.findCause(e);
            if ("andromeda".equalsIgnoreCase(cause) || "dark-matter".equalsIgnoreCase(cause)) {
                throw new AndromedaException(true, "Failed to initialize Andromeda. Please report this to: " + FabricLoader.getInstance().getModContainer("andromeda").orElseThrow().getMetadata().getContact().get("issues"), e);
            } else if (cause != null && !cause.isBlank()) {
                Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(cause);
                if (mod.isPresent()) {
                    if (mod.get().getMetadata().getContact().asMap().containsKey("issues")) {
                        throw new AndromedaException(false, "Failed to initialize Andromeda due to errors provided by: " + cause + ".\n Please report this to: " + mod.get().getMetadata().getContact().get("issues"), e);
                    }
                    throw new AndromedaException(false, "Failed to initialize Andromeda due to errors provided by:" + cause, e);
                }
            }
            throw new AndromedaException(true, "Failed to initialize Andromeda.", e);
        }
    }

    private void initMain() {
        AndromedaReporter.registerCrashHandler();
        BlockRegistry.register();
        ItemRegistry.register();
        EntityTypeRegistry.register();
        ServerSideNetworking.register();
        ResourceConditionRegistry.register();
        ScreenHandlerRegistry.register();
        TagRegistry.register();

        LEAF_SLOWNESS = new EntityAttributeModifier(UUID.fromString("f72625eb-d4c4-4e1d-8e5c-1736b9bab349"), "Leaf Slowness", -0.3, EntityAttributeModifier.Operation.MULTIPLY_BASE);
        KNOCKOFF_TOTEM_PARTICLE = FabricParticleTypes.simple();

        Registry.register(Registry.PARTICLE_TYPE, new Identifier(SharedConstants.MODID, "knockoff_totem_particles"), KNOCKOFF_TOTEM_PARTICLE);

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
                AdvancementGeneration.generateRecipeAdvancements(server);
                server.getPlayerManager().getPlayerList().forEach(entity -> server.getPlayerManager().getAdvancementTracker(entity).reload(server.getAdvancementLoader()));
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (CONFIG.damageBackport) DamageCommand.register(dispatcher);
        });
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
    }

}
