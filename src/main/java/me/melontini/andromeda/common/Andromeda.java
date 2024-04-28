package me.melontini.andromeda.common;//common between modules, not environments.

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import lombok.Getter;
import me.melontini.andromeda.base.AndromedaConfig;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.events.ConfigGsonEvent;
import me.melontini.andromeda.base.util.ConfigHandler;
import me.melontini.andromeda.common.config.ScopedConfigs;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.Debug;
import me.melontini.andromeda.util.commander.CommanderNumberIntermediary;
import me.melontini.andromeda.util.commander.ConstantNumberIntermediary;
import me.melontini.andromeda.util.commander.NumberIntermediary;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static me.melontini.andromeda.util.CommonValues.MODID;

public class Andromeda {

    public static final Identifier VERIFY_MODULES = new Identifier(MODID, "verify_modules");
    @Nullable private static Andromeda INSTANCE;

    public static final Keeper<ItemGroup> GROUP = Keeper.create();

    private static final Supplier<ConfigHandler> ROOT_HANDLER = Suppliers.memoize(() -> (ConfigHandler) FabricLoader.getInstance().getObjectShare().get("andromeda:root_handler"));

    public static ConfigHandler rootHandler() {
        return ROOT_HANDLER.get();
    }

    public static <T extends Module.BaseConfig> ConfigHandler.Entry<T> getConfig(Class<? extends Module<T>> cls) {
        return rootHandler().get(cls);
    }

    public static  <T extends Module.BaseConfig> ConfigHandler.Entry<T> getConfig(Module<T> module) {
        return rootHandler().get(module);
    }

    @Getter
    private @Nullable MinecraftServer currentServer;

    public static void preMain() {
        ConfigGsonEvent.BUS.listen(builder -> {
            Codec<NumberIntermediary> codec = (Codec<NumberIntermediary>) Support.fallback("commander", () -> CommanderNumberIntermediary.CODEC, () -> ConstantNumberIntermediary.CODEC);
            builder.registerTypeAdapter(NumberIntermediary.class, ConfigHandler.context(codec));
        });
    }

    public static void init() {
        var instance = new Andromeda();
        instance.onInitialize(ModuleManager.get());
        Support.share("andromeda:main", instance);
        INSTANCE = instance;
    }

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    private void onInitialize(ModuleManager manager) {
        ResourceConditions.register(id("items_registered"), object -> JsonHelper.getArray(object, "values")
                .asList().stream().filter(JsonElement::isJsonPrimitive)
                .allMatch(e -> CommonRegistries.items().containsId(Identifier.tryParse(e.getAsString()))));

        AndromedaItemGroup.Acceptor acceptor = (module, main, stack) -> {
            if (!stack.isEmpty()) ItemGroupEvents.modifyEntriesEvent(main).register(entries -> entries.add(stack));
        };
        AndromedaItemGroup.getAcceptors().forEach(consumer -> consumer.accept(acceptor));
        if (AndromedaConfig.get().itemGroup) GROUP.init(AndromedaItemGroup.create());

        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.currentServer = null);

        ScopedConfigs.init();

        if (!AndromedaConfig.get().sideOnlyMode) {
            ServerLoginNetworking.registerGlobalReceiver(VERIFY_MODULES, (server, handler, understood, buf, synchronizer, responseSender) -> {
                if (Debug.Keys.SKIP_SERVER_MODULE_CHECK.isPresent()) return;

                Set<String> modules = manager.loaded().stream()
                        .map(Module::meta).filter(m -> m.environment().isBoth())
                        .map(Module.Metadata::id).collect(ImmutableSet.toImmutableSet());
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
                    Set<String> disable = Sets.difference(clientModules, modules);
                    Set<String> enable = Sets.difference(modules, clientModules);

                    if (!disable.isEmpty() || !enable.isEmpty()) {
                        handler.disconnect(TextUtil.translatable("andromeda.disconnected.module_mismatch",
                                Arrays.toString(disable.toArray()), Arrays.toString(enable.toArray())));
                    }
                }));
            });
            ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> sender.sendPacket(VERIFY_MODULES, PacketByteBufs.create()));
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
