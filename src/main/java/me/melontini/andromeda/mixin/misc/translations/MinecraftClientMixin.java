package me.melontini.andromeda.mixin.misc.translations;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.andromeda.util.translations.TranslationUpdater;
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
@MixinRelatedConfigOption("autoUpdateTranslations")
public class MinecraftClientMixin {
    @Shadow @Final public GameOptions options;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackManager;scanPacks()V", shift = At.Shift.BEFORE), method = "reloadResources(Z)Ljava/util/concurrent/CompletableFuture;")
    private void andromeda$downloadLangFiles(boolean force, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (!Andromeda.CONFIG.autoUpdateTranslations) return;
        TranslationUpdater.onResourceReload(this.options.language);
    }
}
