package me.melontini.andromeda.mixin.misc.translations;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.misc.translations.TranslationUpdater;
import me.melontini.andromeda.modules.misc.translations.Translations;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
class MinecraftClientMixin {
    @Unique
    private static final Translations am$trans = ModuleManager.quick(Translations.class);

    @Shadow @Final public GameOptions options;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackManager;scanPacks()V", shift = At.Shift.BEFORE), method = "reloadResources(Z)Ljava/util/concurrent/CompletableFuture;")
    private void andromeda$downloadLangFiles(boolean force, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (!am$trans.config().enabled) return;
        TranslationUpdater.onResourceReload(this.options.language);
    }
}
