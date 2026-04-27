package dev.zenfyr.andromeda.common.mixin.configs;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.config.DataConfigs;
import dev.zenfyr.andromeda.common.config.handler.GameConfigHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
abstract class ServerWorldMixin extends Level implements DataConfigs.AttachmentGetter {

  protected ServerWorldMixin(
      WritableLevelData writableLevelData,
      ResourceKey<Level> resourceKey,
      RegistryAccess registryAccess,
      Holder<DimensionType> holder,
      boolean bl,
      boolean bl2,
      long l,
      int i) {
    super(writableLevelData, resourceKey, registryAccess, holder, bl, bl2, l, i);
  }

  @Shadow
  @NotNull public abstract MinecraftServer getServer();

  @Unique private GameConfigHandler andromeda$configs;

  @Inject(
      at =
          @At(
              value = "FIELD",
              target =
                  "Lnet/minecraft/server/level/ServerLevel;chunkSource:Lnet/minecraft/server/level/ServerChunkCache;",
              ordinal = 0,
              shift = At.Shift.AFTER,
              opcode = Opcodes.PUTFIELD),
      method = "<init>")
  private void andromeda$initStates(CallbackInfo ci) {
    var manager = ModuleManager.get();
    this.andromeda$configs = new GameConfigHandler(
        manager,
        Andromeda.GAME,
        getServer().storageSource.getDimensionPath(this.dimension()).resolve("world_config"),
        RegisterConfigEvent.GAME);

    DataConfigs.get(this.getServer()).applyConfigs(this, this.dimension().location());
  }

  @Override
  public <T extends BaseConfig> T am$get(ConfigDefinition<T> module) {
    return this.andromeda$configs.get(module);
  }

  @Override
  public GameConfigHandler andromeda$getConfigs() {
    return andromeda$configs;
  }
}
