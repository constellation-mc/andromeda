package dev.zenfyr.andromeda.modules.misc.translations.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.zenfyr.andromeda.modules.misc.translations.Translations;
import dev.zenfyr.pulsar.api.util.TextUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReloadableResourceManager.class)
abstract class ReloadableResourceManagerMixin {

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
      @Local(argsOnly = true, name = "resourcePacks") LocalRef<List<PackResources>> packs) {
    if (this.type != PackType.CLIENT_RESOURCES) return;

    packs.set(new ArrayList<>(packs.get()));
    packs
        .get()
        .add(new PathPackResources(
            new PackLocationInfo(
                "Andromeda Translations",
                TextUtil.literal("Andromeda Translations"),
                PackSource.BUILT_IN,
                Optional.empty()),
            Translations.TRANSLATION_PACK));
  }
}
