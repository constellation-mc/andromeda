package me.melontini.andromeda.mixin.misc.translations;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.andromeda.util.translations.TranslationUpdater;
import net.minecraft.resource.*;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableResourceManagerImpl.class)
@MixinRelatedConfigOption("autoUpdateTranslations")
public class ReloadableResourceManagerImplMixin {
    @Shadow @Final private ResourceType type;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/LifecycledResourceManager;close()V", shift = At.Shift.AFTER), method = "reload")
    private void andromeda$injectDownloadedTranslations(CallbackInfoReturnable<ResourceReload> cir, @Local(argsOnly = true) LocalRef<List<ResourcePack>> packs) {
        if (this.type != ResourceType.CLIENT_RESOURCES) return;
        if (!Andromeda.CONFIG.autoUpdateTranslations) return;

        packs.set(new ArrayList<>(packs.get()));
        packs.get().add(new DirectoryResourcePack(TranslationUpdater.TRANSLATION_PACK.toFile()) {
            @Override
            public String getName() {
                return "Andromeda Translations";
            }

            @Nullable
            @Override
            public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) {
                return null;
            }
        });
    }
}
