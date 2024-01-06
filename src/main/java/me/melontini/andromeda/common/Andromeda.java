package me.melontini.andromeda.common;//common between modules, not environments.

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.AndromedaConfig;
import me.melontini.andromeda.common.config.DataConfigs;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.common.util.AndromedaPackets;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.WorldSavePath;

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
        Support.share("andromeda:main", INSTANCE);
    }

    private void onInitialize() {
        Common.bootstrap();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DataConfigs());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (DataConfigs.CONFIGS != null) DataConfigs.CONFIGS = null;
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var list = ModuleManager.get().loaded().stream().filter(module -> module.config().scope == Module.BaseConfig.Scope.DIMENSION).toList();
            server.getWorlds().forEach(world -> ModuleManager.get().cleanConfigs(server.session.getWorldDirectory(world.getRegistryKey()).resolve("world_config/andromeda"), list));
            ModuleManager.get().cleanConfigs(server.session.getDirectory(WorldSavePath.ROOT).resolve("config/andromeda"),
                    ModuleManager.get().loaded().stream().filter(module -> module.config().scope == Module.BaseConfig.Scope.WORLD).toList());
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) DataConfigs.apply(server);
        });

        if (!AndromedaConfig.get().sideOnlyMode) {
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
