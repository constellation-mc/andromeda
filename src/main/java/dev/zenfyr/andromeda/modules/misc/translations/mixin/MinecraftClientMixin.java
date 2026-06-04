package dev.zenfyr.andromeda.modules.misc.translations.mixin;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.modules.misc.translations.Client;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
abstract class MinecraftClientMixin {

  @Shadow
  @Final
  public Options options;

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/server/packs/repository/PackRepository;reload()V",
              shift = At.Shift.BEFORE),
      method =
          "reloadResourcePacks(ZLnet/minecraft/client/Minecraft$GameLoadCookie;)Ljava/util/concurrent/CompletableFuture;")
  private void andromeda$downloadLangFiles(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
    Client.onResourceReload(this.options.languageCode, ModuleManager.get());
  }
}
