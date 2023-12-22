package me.melontini.andromeda.common.config;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.minecraft.world.PersistentStateHelper;
import me.melontini.dark_matter.api.minecraft.world.interfaces.DeserializableState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.IdentityHashMap;
import java.util.Map;

public class ScopedConfigs {

    public static <T extends BasicConfig> T get(World world, Class<? extends Module<T>> cls) {
        return get(world, ModuleManager.quick(cls));
    }

    public static <T extends BasicConfig> T get(World world, Module<T> module) {
        if (world instanceof ServerWorld sw) {
            return switch (module.config().scope) {
                case GLOBAL -> module.config();
                case WORLD -> get(sw.getServer().getWorld(World.OVERWORLD)).get(module);
                case DIMENSION -> get(sw).get(module);
            };
        }
        AndromedaLog.error("Scoped configs requested on client! Returning un-scoped! Module: %s".formatted(module.meta().id()));
        return module.config();
    }

    @SuppressWarnings("UnstableApiUsage")
    public static State get(ServerWorld world) {
        return PersistentStateHelper.getOrCreate(world, State::new, "andromeda_configs_dummy");
    }

    public interface WorldExtension {
        default <T extends BasicConfig> T am$get(Class<? extends Module<T>> cls) {
            if (this instanceof World w) {
                return ScopedConfigs.get(w, cls);
            }
            throw new IllegalStateException();
        }

        default <T extends BasicConfig> T am$get(Module<T> module) {
            if (this instanceof World w) {
                return ScopedConfigs.get(w, module);
            }
            throw new IllegalStateException();
        }
    }

    public static class State extends PersistentState implements DeserializableState {

        private final Map<Module<?>, BasicConfig> configs = new IdentityHashMap<>();

        public <T extends BasicConfig> T get(Module<T> module) {
            return (T) configs.get(module);
        }

        public synchronized void addConfig(Module<?> module, BasicConfig config) {
            configs.put(module, config);
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            return nbt;
        }

        @Override
        public void readNbt(NbtCompound nbt) {
        }
    }
}
