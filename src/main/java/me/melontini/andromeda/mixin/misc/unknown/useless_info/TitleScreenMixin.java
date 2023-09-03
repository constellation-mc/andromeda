package me.melontini.andromeda.mixin.misc.unknown.useless_info;

import me.melontini.andromeda.client.AndromedaClient;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
@MixinRelatedConfigOption("unknown")
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/SplashTextResourceSupplier;get()Lnet/minecraft/client/gui/screen/SplashTextRenderer;"), method = "init")
    private void andromeda$init(CallbackInfo ci) {
        AndromedaClient.get().DEBUG_SPLASH = "Welcome to 1.20!";
    }
}
