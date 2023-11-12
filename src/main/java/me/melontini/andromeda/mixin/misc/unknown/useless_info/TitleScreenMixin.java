package me.melontini.andromeda.mixin.misc.unknown.useless_info;

import me.melontini.andromeda.modules.misc.unknown.Unknown;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.andromeda.util.annotations.MixinEnvironment;
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
@Feature("unknown")
class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/SplashTextResourceSupplier;get()Ljava/lang/String;"), method = "init")
    private void andromeda$init(CallbackInfo ci) {
        Unknown.DEBUG_SPLASH = this.client.getSplashTextLoader().get();
    }
}
