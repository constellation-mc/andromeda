package me.melontini.andromeda.common;

import static me.melontini.andromeda.util.AndromedaConstants.MODID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import lombok.Getter;
import me.melontini.andromeda.bootstrap.ModuleManager;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.common.config.DataConfigs;
import me.melontini.andromeda.common.config.GsonBuilderEvent;
import me.melontini.andromeda.common.config.handler.MultiConfigHandler;
import me.melontini.andromeda.common.util.AndromedaItemGroup;
import me.melontini.andromeda.common.util.GsonCodecContext;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.common.util.commander.IntermediaryTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class Andromeda implements ModInitializer {

  private static Andromeda instance;

  public static final MultiConfigHandler MAIN;
  public static final MultiConfigHandler GAME;

  public static final Keeper<ItemGroup> GROUP = Keeper.create();

  @Getter
  private @Nullable MinecraftServer currentServer;

  static {
    GsonBuilderEvent.BUS.listen(Andromeda::appendCommonGsonTypes);

    MAIN = new MultiConfigHandler(
        ModuleManager.get(),
        FabricLoader.getInstance().getConfigDir(),
        "main",
        RegisterConfigEvent.MAIN);

    GAME = new MultiConfigHandler(
        ModuleManager.get(),
        FabricLoader.getInstance().getConfigDir(),
        "game",
        RegisterConfigEvent.GAME);
  }

  public static Identifier id(String path) {
    return new Identifier(MODID, path);
  }

  public static <T> RegistryKey<T> key(RegistryKey<? extends Registry<T>> registry, String path) {
    return RegistryKey.of(registry, id(path));
  }

  public static Gson buildGson() {
    GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
    GsonBuilderEvent.BUS.invoker().acceptGsonBuilder(builder);
    return builder.create();
  }

  @Override
  public void onInitialize() {
    instance = this;
    var manager = ModuleManager.get();

    // Load and save configs. Saving ensures that the `main` part is created.
    MAIN.loadAll();
    MAIN.saveAll();

    InitEvents.MAIN.invoker().onModuleMainInit().runEntrypoint();

    ResourceConditions.register(
        id("items_registered"), object -> JsonHelper.getArray(object, "values").asList().stream()
            .filter(JsonElement::isJsonPrimitive)
            .allMatch(e -> Registries.ITEM.containsId(new Identifier(e.getAsString()))));

    ResourceConditions.register(
        id("modules_loaded"), object -> JsonHelper.getArray(object, "values").asList().stream()
            .filter(JsonElement::isJsonPrimitive)
            .allMatch(e -> ModuleManager.get().get(e.getAsString()).isPresent()));

    GROUP.init(AndromedaItemGroup.create());

    // Keep a reference to the currently running server.
    ServerLifecycleEvents.SERVER_STARTING.register(server -> this.currentServer = server);
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.currentServer = null);

    // Init the data pack config system
    DataConfigs.init(manager);
  }

  void onMergedEntryPoint() {
    var manager = ModuleManager.get();

    GAME.loadAll();
    GAME.saveAll();

    InitEvents.MERGED.invoker().onModuleMergedInit().runEntrypoint();
  }

  public static Andromeda get() {
    return instance;
  }

  public static void appendCommonGsonTypes(GsonBuilder builder) {
    IntermediaryTypes.initialize(builder); // Commander support

    builder.registerTypeHierarchyAdapter(Identifier.class, GsonCodecContext.of(Identifier.CODEC));
    builder.registerTypeHierarchyAdapter(Identifier.class, GsonCodecContext.of(Identifier.CODEC));
    builder.registerTypeHierarchyAdapter(
        StatusEffect.class, GsonCodecContext.of(Registries.STATUS_EFFECT.getCodec()));
    builder.registerTypeHierarchyAdapter(
        Item.class, GsonCodecContext.of(Registries.ITEM.getCodec()));
    builder.registerTypeHierarchyAdapter(
        Block.class, GsonCodecContext.of(Registries.BLOCK.getCodec()));
  }
}
