package me.melontini.andromeda.modules.misc.translations.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import java.util.ArrayList;
import java.util.List;
import me.melontini.andromeda.modules.misc.translations.Translations;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReloadableResourceManager.class)
abstract class ReloadableResourceManagerImplMixin {

  @Shadow
  @Final
  private PackType type;

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/server/packs/resources/CloseableResourceManager;close()V",
              shift = At.Shift.AFTER),
      method = "createReload")
  private void andromeda$injectDownloadedTranslations(
      CallbackInfoReturnable<ReloadInstance> cir,
      @Local(argsOnly = true) LocalRef<List<PackResources>> packs) {
    if (this.type != PackType.CLIENT_RESOURCES) return;

    packs.set(new ArrayList<>(packs.get()));
    packs
        .get()
        .add(
            new PathPackResources(
                "Andromeda Translations", Translations.TRANSLATION_PACK, true) {
              @Nullable @Override
              public <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
                return null;
              }
            });
  }
}
