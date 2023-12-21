package me.melontini.andromeda.common;

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

import java.util.HashMap;
import java.util.Map;

//TODO:
// a not-so-painful way to edit those configs. (Outside of KubeJS)
public class ScopedConfigs {

    public static <T extends BasicConfig> T get(World world, Class<? extends Module<T>> cls) {
        Module<T> module = ModuleManager.quick(cls);
        if (world instanceof ServerWorld sw) {
            return switch (module.config().scope) {
                case GLOBAL -> module.config();
                case WORLD -> get(sw.getServer().getWorld(World.OVERWORLD)).get(module);
                case DIMENSION -> get(sw).get(module);
            };
        }
        AndromedaLog.error("Scoped configs requested on client! Returning un-scoped!");
        return module.config();
    }

    public static State get(ServerWorld world) {
        return PersistentStateHelper.getOrCreate(world, State::new, "andromeda_configs_dummy");
    }

    public static class State extends PersistentState implements DeserializableState {

        private final Map<Module<?>, BasicConfig> configs = new HashMap<>();

        public <T extends BasicConfig> T get(Module<T> module) {
            return (T) configs.get(module);
        }

        void addConfig(Module<?> module, BasicConfig config) {
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
