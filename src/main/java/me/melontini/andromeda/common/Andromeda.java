package me.melontini.andromeda.common;//common between modules, not environments.

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.Getter;
import me.melontini.andromeda.base.AndromedaConfig;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.config.DataConfigs;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.data.loading.ServerReloadersEvent;
import me.melontini.dark_matter.api.item_group.ItemGroupBuilder;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.melontini.andromeda.common.registries.Common.id;
import static me.melontini.andromeda.util.CommonValues.MODID;

public class Andromeda {

    public static final Identifier VERIFY_MODULES = new Identifier(MODID, "verify_modules");
    @Nullable private static Andromeda INSTANCE;

    public final ItemGroup group = ItemGroupBuilder.create(id("group"))
            .entries(entries -> {
                Map<Module<?>, List<ItemStack>> stackMap = new LinkedHashMap<>();
                AndromedaItemGroup.Acceptor acceptor = (module, stack) -> {
                    if (!stack.isEmpty()) {
                        stackMap.computeIfAbsent(module, module1 -> new ArrayList<>()).add(stack);
                    }
                };
                AndromedaItemGroup.getAcceptors().forEach(consumer -> consumer.accept(acceptor));

                Map<Module<?>, List<ItemStack>> small = new LinkedHashMap<>();
                Map<Module<?>, List<ItemStack>> big = new LinkedHashMap<>();

                if (stackMap.isEmpty()) {
                    entries.add(Items.BARRIER);
                    return;
                }

                stackMap.forEach((module, itemStacks) -> {
                    if (itemStacks.size() > 2) {
                        big.put(module, itemStacks);
                    } else if (!itemStacks.isEmpty()) {
                        small.put(module, itemStacks);
                    }
                });

                if (small.isEmpty() && big.isEmpty()) {
                    entries.add(Items.BARRIER);
                    return;
                }

                List<ItemStack> stacks = new ArrayList<>();
                small.forEach((m, itemStacks) -> {
                    ItemStack sign = new ItemStack(Items.SPRUCE_SIGN);
                    sign.setCustomName(TextUtil.translatable("config.andromeda.%s".formatted(m.meta().dotted())));
                    stacks.add(sign);
                    stacks.addAll(itemStacks);
                    stacks.add(ItemStack.EMPTY);
                });
                entries.appendStacks(stacks);

                big.forEach((m, itemStacks) -> {
                    ItemStack sign = new ItemStack(Items.SPRUCE_SIGN);
                    sign.setCustomName(TextUtil.translatable("config.andromeda.%s".formatted(m.meta().dotted())));
                    itemStacks.add(0, sign);
                    entries.appendStacks(itemStacks);
                });
            })
            .displayName(TextUtil.translatable("itemGroup.andromeda.items")).optional().orElseThrow();

    @Getter
    private @Nullable MinecraftServer currentServer;

    public static void init() {
        var instance = new Andromeda();
        instance.onInitialize(ModuleManager.get());
        Support.share("andromeda:main", instance);
        INSTANCE = instance;
    }

    private void onInitialize(ModuleManager manager) {
        Common.bootstrap();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.currentServer = null);

        ServerReloadersEvent.EVENT.register(context -> context.register(new DataConfigs()));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var list = manager.loaded().stream().filter(module -> module.config().scope.isDimension()).toList();
            server.getWorlds().forEach(world -> manager.cleanConfigs(server.session.getWorldDirectory(world.getRegistryKey()).resolve("world_config/andromeda"), list));
            manager.cleanConfigs(server.session.getDirectory(WorldSavePath.ROOT).resolve("config/andromeda"),
                    manager.loaded().stream().filter(module -> module.config().scope.isWorld()).toList());
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) DataConfigs.get(server).apply(server);
        });

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
