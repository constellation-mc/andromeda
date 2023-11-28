package me.melontini.andromeda.modules.misc.unknown.mixin.useless_info;

import me.melontini.andromeda.base.annotations.MixinEnvironment;
import me.melontini.andromeda.modules.misc.unknown.Unknown;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinEnvironment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/SplashTextResourceSupplier;get()Ljava/lang/String;"), method = "init")
    private void andromeda$init(CallbackInfo ci) {
        Unknown.DEBUG_SPLASH = this.client.getSplashTextLoader().get();
    }
}
