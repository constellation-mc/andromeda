package me.melontini.andromeda;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.content.commands.DamageCommand;
import me.melontini.andromeda.content.managers.EnderDragonManager;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.util.AdvancementGeneration;
import me.melontini.andromeda.util.AndromedaReporter;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.data.EggProcessingData;
import me.melontini.andromeda.util.data.PlantTemperatureData;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.melontini.dark_matter.api.minecraft.world.PersistentStateHelper;
import me.melontini.dark_matter.api.minecraft.world.interfaces.TickableState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Andromeda {

    private static Andromeda INSTANCE;

    public Map<Block, PlantTemperatureData> PLANT_DATA = new HashMap<>();
    public Map<Item, EggProcessingData> EGG_DATA = new HashMap<>();

    public DefaultParticleType KNOCKOFF_TOTEM_PARTICLE;

    public EntityAttributeModifier LEAF_SLOWNESS;

    public DamageSource AGONY;

    public static DamageSource bricked(@Nullable Entity attacker) {
        return new BrickedDamageSource(attacker);
    }

    public static void init() {
        INSTANCE = new Andromeda();
        INSTANCE.onInitialize();
    }

    private void onInitialize() {
        EntrypointRunner.runEntrypoint("andromeda:pre-main", ModInitializer.class, ModInitializer::onInitialize);

        AndromedaReporter.initCrashHandler();
        Common.bootstrap();

        ServerWorldEvents.LOAD.register((server, world) -> {
            if (Config.get().dragonFight.fightTweaks) if (world.getRegistryKey() == World.END)
                EnderDragonManager.get(world);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            Andromeda.get().PLANT_DATA.clear();
            Andromeda.get().EGG_DATA.clear();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ItemBehaviorManager.clear();
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (Config.get().dragonFight.fightTweaks) if (world.getRegistryKey() == World.END) {
                PersistentStateHelper.consumeIfLoaded(world, EnderDragonManager.ID,
                        (world1, s) -> EnderDragonManager.get(world1), TickableState::tick);
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (Config.get().recipeAdvancementsGeneration.enable) {
                Config.run(() -> {
                    AdvancementGeneration.generateRecipeAdvancements(server);
                    server.getPlayerManager().getPlayerList().forEach(entity -> server.getPlayerManager().getAdvancementTracker(entity).reload(server.getAdvancementLoader()));
                }, "autogenRecipeAdvancements.autogenRecipeAdvancements");
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (Config.get().damageBackport) Config.run(() -> DamageCommand.register(dispatcher), "damageBackport");
        });

        EntrypointRunner.runEntrypoint("andromeda:post-main", ModInitializer.class, ModInitializer::onInitialize);
    }

    @Override
    public String toString() {
        return "Andromeda{version=" + CommonValues.version() + "}";
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
