package me.melontini.andromeda.common.mixin.configs;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.ConfigHandler;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.config.DataConfigs;
import me.melontini.andromeda.common.config.ScopedConfigs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin implements ScopedConfigs.AttachmentGetter {

    @Shadow @Final public LevelStorage.Session session;
    @Unique
    private ConfigHandler andromeda$configs;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;session:Lnet/minecraft/world/level/storage/LevelStorage$Session;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER), method = "<init>")
    private void andromeda$initStates(CallbackInfo ci) {
        this.andromeda$configs = new ConfigHandler(
                this.session.getDirectory(WorldSavePath.ROOT).resolve("config"),
                ModuleManager.get().loaded().stream().filter(m -> Andromeda.getConfig(m).e.scope.isWorld()).toList());

        this.andromeda$configs.setRoot(Andromeda.rootHandler());
        DataConfigs.get((MinecraftServer) (Object) this).apply(this, DataConfigs.DEFAULT);
    }

    @Override
    public ConfigHandler andromeda$getConfigs() {
        return this.andromeda$configs;
    }
}
