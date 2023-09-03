package me.melontini.andromeda.mixin.misc;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.melontini.andromeda.util.AdvancementGeneration.generateRecipeAdvancements;

@Mixin(PlayerManager.class)
@MixinRelatedConfigOption("autogenRecipeAdvancements.autogenRecipeAdvancements")
public class PlayerManagerMixin {

    @Shadow @Final private MinecraftServer server;

    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;", ordinal = 0, shift = At.Shift.BEFORE), method = "onDataPacksReloaded")
    private void andromeda$reload(CallbackInfo ci) {
        //we don't sync until our advancements have been generated
        if (Config.get().autogenRecipeAdvancements.autogenRecipeAdvancements) generateRecipeAdvancements(server);
    }
}
