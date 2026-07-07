package dev.zenfyr.andromeda.modules.misc.translations.mixin;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.modules.misc.translations.TranslationsClient;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/server/packs/repository/PackRepository;reload()V",
              shift = At.Shift.BEFORE),
      method =
          "reloadResourcePacks(ZLnet/minecraft/client/Minecraft$GameLoadCookie;)Ljava/util/concurrent/CompletableFuture;")
  private void andromeda$downloadLangFiles(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
    Minecraft self = (Minecraft) (Object) this;
    TranslationsClient.onResourceReload(self.options.languageCode, ModuleManager.get());
  }
}
