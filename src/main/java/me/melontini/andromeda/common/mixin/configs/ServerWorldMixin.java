package me.melontini.andromeda.common.mixin.configs;

import me.melontini.andromeda.common.config.DataConfigs;
import me.melontini.andromeda.common.config.ScopedConfigs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
abstract class ServerWorldMixin implements ScopedConfigs.AttachmentGetter {

    @Shadow @NotNull public abstract MinecraftServer getServer();

    @Unique
    private final ScopedConfigs.Attachment andromeda$configs = new ScopedConfigs.Attachment();

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ServerWorld;chunkManager:Lnet/minecraft/server/world/ServerChunkManager;", ordinal = 0, shift = At.Shift.AFTER), method = "<init>")
    private void andromeda$initStates(CallbackInfo ci) {
        DataConfigs.get(this.getServer()).apply((ServerWorld) (Object) this);
    }

    @Override
    public ScopedConfigs.Attachment andromeda$getConfigs() {
        return andromeda$configs;
    }
}
