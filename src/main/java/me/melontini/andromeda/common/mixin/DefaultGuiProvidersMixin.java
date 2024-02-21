package me.melontini.andromeda.common.mixin;

import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.common.util.TranslationKeyProvider;
import me.melontini.dark_matter.api.base.util.mixin.annotations.MixinPredicate;
import me.melontini.dark_matter.api.base.util.mixin.annotations.Mod;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.shedaniel.autoconfig.gui.DefaultGuiProviders;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@MixinPredicate(mods = @Mod("cloth-config"))
@SpecialEnvironment(Environment.CLIENT)
@Mixin(DefaultGuiProviders.class)
abstract class DefaultGuiProvidersMixin {

    @Inject(at = @At("HEAD"), method = "lambda$static$0(Ljava/lang/Enum;)Lnet/minecraft/text/Text;", cancellable = true)
    private static void andromeda$injectTranslations(Enum<?> t, CallbackInfoReturnable<Text> cir) {
        if (t instanceof TranslationKeyProvider provider) {//DIY API
            provider.getTranslationKey().ifPresent(string -> cir.setReturnValue(TextUtil.translatable(string)));
        }
    }
}
