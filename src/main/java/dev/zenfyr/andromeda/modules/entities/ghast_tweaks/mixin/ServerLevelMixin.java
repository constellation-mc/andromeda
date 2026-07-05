package dev.zenfyr.andromeda.modules.entities.ghast_tweaks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.entities.ghast_tweaks.GhastExplosionDuck;
import dev.zenfyr.andromeda.modules.entities.ghast_tweaks.GhastTweaks;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {

  protected ServerLevelMixin(
      WritableLevelData levelData,
      ResourceKey<Level> dimension,
      RegistryAccess registryAccess,
      Holder<DimensionType> dimensionTypeRegistration,
      boolean isClientSide,
      boolean isDebug,
      long biomeZoomSeed,
      int maxChainedNeighborUpdates) {
    super(
        levelData,
        dimension,
        registryAccess,
        dimensionTypeRegistration,
        isClientSide,
        isDebug,
        biomeZoomSeed,
        maxChainedNeighborUpdates);
  }

  @Inject(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerExplosion;explode()I"),
      method = "explode")
  private void andromeda$modExplosion(
      CallbackInfo ci,
      @Local(argsOnly = true, name = "source") Entity source,
      @Local(name = "explosion") ServerExplosion explosion) {
    if ((source instanceof Fireball fb)
        && fb.getOwner() instanceof Ghast
        && this.am$get(GhastTweaks.CONFIG).fireBallsConvertObsidian) {
      ((GhastExplosionDuck) explosion).andromeda$convertObsidian(true);
    }
  }
}
