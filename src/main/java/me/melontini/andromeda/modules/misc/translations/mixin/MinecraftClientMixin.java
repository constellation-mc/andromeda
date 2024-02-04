package me.melontini.andromeda.modules.misc.translations.mixin;

import me.melontini.andromeda.modules.misc.translations.client.Client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {

    @Shadow @Final public GameOptions options;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackManager;scanPacks()V", shift = At.Shift.BEFORE), method = "reloadResources(ZLnet/minecraft/client/MinecraftClient$LoadingContext;)Ljava/util/concurrent/CompletableFuture;")
    private void andromeda$downloadLangFiles(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        Client.onResourceReload(this.options.language);
    }
}
