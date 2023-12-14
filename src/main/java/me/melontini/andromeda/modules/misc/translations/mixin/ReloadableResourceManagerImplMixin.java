package me.melontini.andromeda.modules.misc.translations.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.melontini.andromeda.modules.misc.translations.TranslationUpdater;
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
class ReloadableResourceManagerImplMixin {

    @Shadow @Final private ResourceType type;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/LifecycledResourceManager;close()V", shift = At.Shift.AFTER), method = "reload")
    private void andromeda$injectDownloadedTranslations(CallbackInfoReturnable<ResourceReload> cir, @Local(argsOnly = true) LocalRef<List<ResourcePack>> packs) {
        if (this.type != ResourceType.CLIENT_RESOURCES) return;

        packs.set(new ArrayList<>(packs.get()));
        packs.get().add(new DirectoryResourcePack("Andromeda Translations", TranslationUpdater.TRANSLATION_PACK, true) {
            @Nullable
            @Override
            public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) {
                return null;
            }
        });
    }
}
