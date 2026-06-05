package dev.zenfyr.andromeda.common;

import static dev.zenfyr.andromeda.util.AndromedaConstants.MODID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.common.config.DataConfigs;
import dev.zenfyr.andromeda.common.config.GsonBuilderEvent;
import dev.zenfyr.andromeda.common.config.handler.MultiConfigHandler;
import dev.zenfyr.andromeda.common.util.AndromedaItemGroup;
import dev.zenfyr.andromeda.common.util.GsonCodecContext;
import dev.zenfyr.andromeda.common.util.Keeper;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class Andromeda implements ModInitializer {

  private static Andromeda instance;

  public static final MultiConfigHandler MAIN;
  public static final MultiConfigHandler GAME;

  public static final Keeper<CreativeModeTab> GROUP = Keeper.create();

  @Getter
  private @Nullable MinecraftServer currentServer;

  static {
    var manager = ModuleManager.get();

    GsonBuilderEvent.BUS.listen(Andromeda::appendCommonGsonTypes);

    MAIN = new MultiConfigHandler(
        manager, FabricLoader.getInstance().getConfigDir(), "main", RegisterConfigEvent.MAIN);

    GAME = new MultiConfigHandler(
        manager, FabricLoader.getInstance().getConfigDir(), "game", RegisterConfigEvent.GAME);
  }

  public static ResourceLocation id(String path) {
    return new ResourceLocation(MODID, path);
  }

  public static <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String path) {
    return ResourceKey.create(registry, id(path));
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
        id("items_registered"),
        object -> GsonHelper.getAsJsonArray(object, "values").asList().stream()
            .filter(JsonElement::isJsonPrimitive)
            .allMatch(
                e -> BuiltInRegistries.ITEM.containsKey(new ResourceLocation(e.getAsString()))));

    ResourceConditions.register(
        id("modules_loaded"),
        object -> GsonHelper.getAsJsonArray(object, "values").asList().stream()
            .filter(JsonElement::isJsonPrimitive)
            .allMatch(e -> manager.get(e.getAsString()).isPresent()));

    GROUP.init(AndromedaItemGroup.create());

    // Keep a reference to the currently running server.
    ServerLifecycleEvents.SERVER_STARTING.register(server -> this.currentServer = server);
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.currentServer = null);

    // Init the data pack config system
    DataConfigs.init(manager);

    if (manager.debug().isMixinAudit()) {
      MixinEnvironment.getCurrentEnvironment().audit();
    }
  }

  public void onMergedEntryPoint(ModuleManager manager) {
    GAME.loadAll();
    GAME.saveAll();

    InitEvents.MERGED.invoker().onModuleMergedInit().runEntrypoint();
  }

  public static Andromeda get() {
    return instance;
  }

  public static void appendCommonGsonTypes(GsonBuilder builder) {
    builder.registerTypeHierarchyAdapter(
        ResourceLocation.class, GsonCodecContext.of(ResourceLocation.CODEC));
    builder.registerTypeHierarchyAdapter(
        MobEffect.class,
        GsonCodecContext.of(BuiltInRegistries.MOB_EFFECT
            .holderByNameCodec()
            .xmap(Holder::value, BuiltInRegistries.MOB_EFFECT::wrapAsHolder)));
    builder.registerTypeHierarchyAdapter(
        Item.class,
        GsonCodecContext.of(BuiltInRegistries.ITEM
            .holderByNameCodec()
            .xmap(Holder::value, BuiltInRegistries.ITEM::wrapAsHolder)));
    builder.registerTypeHierarchyAdapter(
        Block.class,
        GsonCodecContext.of(BuiltInRegistries.BLOCK
            .holderByNameCodec()
            .xmap(Holder::value, BuiltInRegistries.BLOCK::wrapAsHolder)));
  }
}
