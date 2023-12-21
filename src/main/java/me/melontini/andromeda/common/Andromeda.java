package me.melontini.andromeda.common;//common between modules, not environments.

import lombok.SneakyThrows;
import me.melontini.andromeda.base.Debug;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.common.config.DataConfigs;
import me.melontini.andromeda.common.config.ScopedConfigs;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.util.AndromedaPackets;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.CrashHandler;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Andromeda {

    private static Andromeda INSTANCE;

    public static void init() {
        INSTANCE = new Andromeda();
        INSTANCE.onInitialize();
        FabricLoader.getInstance().getObjectShare().put("andromeda:main", INSTANCE);
    }

    @SneakyThrows
    private static <T extends BasicConfig> T loadScoped(Path root, Identifier id, Module<T> module) {
        var manager = module.manager();
        if (Files.exists(manager.resolve(root))) {
            return manager.load(root);
        }
        if (id != null) {
            var data = DataConfigs.CONFIGS.get(id);
            if (data != null) {
                var forModule = data.get(module);
                if (forModule != null) {
                    return (T) forModule;
                }
            }
        }
        return manager.load(FabricLoader.getInstance().getConfigDir());
    }

    private static void prepareForWorld(ServerWorld world, Module<?> module, Path p) {
        ScopedConfigs.State state = ScopedConfigs.get(world);
        BasicConfig config = loadScoped(p, module.config().scope != BasicConfig.Scope.DIMENSION ? null : world.getRegistryKey().getValue(), module);
        state.addConfig(module, config);
        try {
            module.manager().save(p, Utilities.cast(config));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onInitialize() {
        CrashHandler.initCrashHandler();
        Common.bootstrap();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DataConfigs());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (DataConfigs.CONFIGS != null) DataConfigs.CONFIGS = null;
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            MakeSure.notNull(DataConfigs.CONFIGS);

            server.getWorlds().forEach(world -> ModuleManager.get().cleanConfigs(server.session.getWorldDirectory(world.getRegistryKey()).resolve("world_config/andromeda")));
            ModuleManager.get().cleanConfigs(server.session.getDirectory(WorldSavePath.ROOT).resolve("config/andromeda"));

            server.getWorlds().forEach(ScopedConfigs::get);

            for (Module<?> module : ModuleManager.get().all()) {
                if (module.meta().environment() == Environment.CLIENT) continue; //Those are always GLOBAL.

                switch (module.config().scope) {
                    case WORLD -> {
                        ServerWorld world = server.getWorld(World.OVERWORLD);
                        Path p = server.session.getDirectory(WorldSavePath.ROOT).resolve("config");
                        prepareForWorld(world, module, p);
                    }
                    case DIMENSION -> {
                        for (ServerWorld world : server.getWorlds()) {
                            Path p = server.session.getWorldDirectory(world.getRegistryKey()).resolve("world_config");
                            prepareForWorld(world, module, p);
                        }
                    }
                }
            }
        });

        if (!Config.get().sideOnlyMode) {
            ServerLoginNetworking.registerGlobalReceiver(AndromedaPackets.VERIFY_MODULES, (server, handler, understood, buf, synchronizer, responseSender) -> {
                if (Debug.hasKey(Debug.Keys.SKIP_SERVER_MODULE_CHECK)) return;

                Set<String> modules = ModuleManager.get().loaded().stream().filter(m -> m.meta().environment() == Environment.BOTH).map(m -> m.meta().id()).collect(Collectors.toSet());
                if (!understood) {
                    if (!modules.isEmpty())
                        handler.disconnect(TextUtil.translatable("andromeda.disconnected.module_mismatch",
                                Arrays.toString(new String[0]), Arrays.toString(modules.toArray())));
                    return;
                }

                int length = buf.readVarInt();
                Set<String> clientModules = new HashSet<>();
                for (int i = 0; i < length; i++) {
                    clientModules.add(buf.readString());
                }

                synchronizer.waitFor(server.submit(() -> {
                    Set<String> disable = new HashSet<>();
                    for (String clientModule : clientModules) {
                        if (!modules.contains(clientModule)) {
                            disable.add(clientModule);
                        }
                    }

                    Set<String> enable = new HashSet<>();
                    for (String module : modules) {
                        if (!clientModules.contains(module)) {
                            enable.add(module);
                        }
                    }

                    if (!disable.isEmpty() || !enable.isEmpty()) {
                        handler.disconnect(TextUtil.translatable("andromeda.disconnected.module_mismatch",
                                Arrays.toString(disable.toArray()), Arrays.toString(enable.toArray())));
                    }
                }));
            });
            ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> sender.sendPacket(AndromedaPackets.VERIFY_MODULES, PacketByteBufs.create()));
        }
    }

    @Override
    public String toString() {
        return "Andromeda{version=" + CommonValues.version() + "}";
    }

    public static Andromeda get() {
        return Objects.requireNonNull(INSTANCE, "Andromeda not initialized");
    }
}
