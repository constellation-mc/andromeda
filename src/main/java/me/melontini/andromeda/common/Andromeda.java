package me.melontini.andromeda.common;//common between modules, not environments.

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import lombok.Getter;
import me.melontini.andromeda.base.AndromedaConfig;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.events.ConfigGsonEvent;
import me.melontini.andromeda.base.events.ConstructorParametersEvent;
import me.melontini.andromeda.base.util.ConfigHandler;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Promise;
import me.melontini.andromeda.common.config.ScopedConfigs;
import me.melontini.andromeda.common.util.GsonCodecContext;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.Debug;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.util.commander.bool.CommanderBooleanIntermediary;
import me.melontini.andromeda.util.commander.bool.ConstantBooleanIntermediary;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;
import me.melontini.andromeda.util.commander.number.LongIntermediary;
import me.melontini.andromeda.util.commander.number.constant.ConstantDoubleIntermediary;
import me.melontini.andromeda.util.commander.number.constant.ConstantLongIntermediary;
import me.melontini.andromeda.util.commander.number.expression.CommanderDoubleIntermediary;
import me.melontini.andromeda.util.commander.number.expression.CommanderLongIntermediary;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.melontini.andromeda.util.CommonValues.MODID;

public final class Andromeda {

    public static final Identifier VERIFY_MODULES = new Identifier(MODID, "verify_modules");
    @Nullable private static Andromeda INSTANCE;

    public static final Keeper<ItemGroup> GROUP = Keeper.create();

    public static final ConfigHandler ROOT_HANDLER;
    public static final ConfigHandler GAME_HANDLER;

    static {
        ConfigGsonEvent.BUS.listen(builder -> {
            Codec<DoubleIntermediary> doubleCodec = (Codec<DoubleIntermediary>) Support.fallback("commander", () -> CommanderDoubleIntermediary.CODEC, () -> ConstantDoubleIntermediary.CODEC);
            builder.registerTypeHierarchyAdapter(DoubleIntermediary.class, GsonCodecContext.of(doubleCodec));

            Codec<LongIntermediary> longCodec = (Codec<LongIntermediary>) Support.fallback("commander", () -> CommanderLongIntermediary.CODEC, () -> ConstantLongIntermediary.CODEC);
            builder.registerTypeHierarchyAdapter(LongIntermediary.class, GsonCodecContext.of(longCodec));

            Codec<BooleanIntermediary> booleanIntermediaryCodec = (Codec<BooleanIntermediary>) Support.fallback("commander", () -> CommanderBooleanIntermediary.CODEC, () -> ConstantBooleanIntermediary.CODEC);
            builder.registerTypeHierarchyAdapter(BooleanIntermediary.class, GsonCodecContext.of(booleanIntermediaryCodec));

            builder.registerTypeHierarchyAdapter(Identifier.class, GsonCodecContext.of(Identifier.CODEC));
            builder.registerTypeHierarchyAdapter(StatusEffect.class, GsonCodecContext.of(Registries.STATUS_EFFECT.getCodec()));
            builder.registerTypeHierarchyAdapter(Item.class, GsonCodecContext.of(Registries.ITEM.getCodec()));
            builder.registerTypeHierarchyAdapter(Block.class, GsonCodecContext.of(Registries.BLOCK.getCodec()));
        });

        ROOT_HANDLER = new ConfigHandler(FabricLoader.getInstance().getConfigDir(), ConfigState.MAIN, ModuleManager.get().all().stream().map(Promise::get).toList());
        GAME_HANDLER = new ConfigHandler(FabricLoader.getInstance().getConfigDir(), ConfigState.GAME, ModuleManager.get().all().stream().map(Promise::get).toList());
    }

    @Getter
    private @Nullable MinecraftServer currentServer;

    public static void preMain() {
        ConstructorParametersEvent.BUS.listen(module -> {
            var cd = module.getConfigDefinition(ConfigState.MAIN);
            if (cd != null) return Collections.singletonMap(cd.supplier().get(), Andromeda.ROOT_HANDLER.get(cd));
            return Collections.emptyMap();
        });

        ROOT_HANDLER.loadAll();
        ROOT_HANDLER.saveAll();
    }

    public static void init() {
        var instance = new Andromeda();
        instance.onInitialize(ModuleManager.get());
        Support.share("andromeda:main", instance);
        INSTANCE = instance;
    }

    public static void onMerged() {
        GAME_HANDLER.loadAll();
        GAME_HANDLER.saveAll();
    }

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    private void onInitialize(ModuleManager manager) {
        ResourceConditions.register(id("items_registered"), object -> JsonHelper.getArray(object, "values")
                .asList().stream().filter(JsonElement::isJsonPrimitive)
                .allMatch(e -> Registries.ITEM.containsId(Identifier.tryParse(e.getAsString()))));

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
