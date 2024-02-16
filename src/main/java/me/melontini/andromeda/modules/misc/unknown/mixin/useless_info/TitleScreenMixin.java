package me.melontini.andromeda.modules.misc.unknown.mixin.useless_info;

import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.modules.misc.unknown.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SpecialEnvironment(Environment.CLIENT)
@Mixin(TitleScreen.class)
abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/SplashTextResourceSupplier;get()Lnet/minecraft/client/gui/screen/SplashTextRenderer;"), method = "init")
    private void andromeda$init(CallbackInfo ci) {
        var s = MinecraftClient.getInstance().getSplashTextLoader().get();
        if (s != null) Main.DEBUG_SPLASH = s.text;
    }
}
