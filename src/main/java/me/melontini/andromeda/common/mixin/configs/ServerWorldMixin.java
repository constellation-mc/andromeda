package me.melontini.andromeda.common.mixin.configs;

import java.util.function.Supplier;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigHandler;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.config.DataConfigs;
import me.melontini.andromeda.common.config.ScopedConfigs;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
abstract class ServerWorldMixin extends World implements ScopedConfigs.AttachmentGetter {

  protected ServerWorldMixin(
      MutableWorldProperties properties,
      RegistryKey<World> registryRef,
      DynamicRegistryManager registryManager,
      RegistryEntry<DimensionType> dimensionEntry,
      Supplier<Profiler> profiler,
      boolean isClient,
      boolean debugWorld,
      long biomeAccess,
      int maxChainedNeighborUpdates) {
    super(
        properties,
        registryRef,
        registryManager,
        dimensionEntry,
        profiler,
        isClient,
        debugWorld,
        biomeAccess,
        maxChainedNeighborUpdates);
  }

  @Shadow
  @NotNull public abstract MinecraftServer getServer();

  @Unique private ConfigHandler andromeda$configs;

  @Inject(
      at =
          @At(
              value = "FIELD",
              target =
                  "Lnet/minecraft/server/world/ServerWorld;chunkManager:Lnet/minecraft/server/world/ServerChunkManager;",
              ordinal = 0,
              shift = At.Shift.AFTER),
      method = "<init>")
  private void andromeda$initStates(CallbackInfo ci) {
    var manager = ModuleManager.get();
    this.andromeda$configs = new ConfigHandler(
        getServer().session.getWorldDirectory(this.getRegistryKey()).resolve("world_config"),
        true,
        ConfigState.GAME,
        Andromeda.GAME_HANDLER,
        manager.loaded().stream()
            .filter(m -> m.getConfigDefinition(ConfigState.GAME) != null)
            .toList());

    DataConfigs.get(this.getServer()).apply(this, this.getRegistryKey().getValue());
  }

  @Override
  public <T extends Module.BaseConfig> T am$get(ConfigDefinition<T> module) {
    return this.andromeda$configs.get(module);
  }

  @Override
  public ConfigHandler andromeda$getConfigs() {
    return andromeda$configs;
  }

  @Override
  public boolean am$isReady() {
    return true;
  }
}
