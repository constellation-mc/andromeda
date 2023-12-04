package me.melontini.andromeda;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.util.AndromedaPackets;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.CrashHandler;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.loader.api.FabricLoader;

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

    private void onInitialize() {
        CrashHandler.initCrashHandler();
        Common.bootstrap();

        if (!Config.get().sideOnlyMode) {
            ServerLoginNetworking.registerGlobalReceiver(AndromedaPackets.VERIFY_MODULES, (server, handler, understood, buf, synchronizer, responseSender) -> {
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
