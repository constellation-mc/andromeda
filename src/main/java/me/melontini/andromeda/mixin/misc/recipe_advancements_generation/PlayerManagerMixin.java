package me.melontini.andromeda.mixin.misc.recipe_advancements_generation;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.misc.recipe_advancements_generation.AdvancementGeneration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.melontini.andromeda.modules.misc.recipe_advancements_generation.Helper.generateRecipeAdvancements;

@Mixin(PlayerManager.class)
class PlayerManagerMixin {
    @Unique
    private static final AdvancementGeneration am$rag = ModuleManager.quick(AdvancementGeneration.class);

    @Shadow @Final private MinecraftServer server;

    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;", ordinal = 0, shift = At.Shift.BEFORE), method = "onDataPacksReloaded")
    private void andromeda$reload(CallbackInfo ci) {
        //we don't sync until our advancements have been generated
        if (am$rag.config().enabled) generateRecipeAdvancements(server);
    }
}
