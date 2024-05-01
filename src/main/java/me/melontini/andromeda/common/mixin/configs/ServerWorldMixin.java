package me.melontini.andromeda.common.mixin.configs;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.ConfigHandler;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.config.DataConfigs;
import me.melontini.andromeda.common.config.ScopedConfigs;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
abstract class ServerWorldMixin extends World implements ScopedConfigs.AttachmentGetter {

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Shadow @NotNull public abstract MinecraftServer getServer();

    @Unique private ConfigHandler andromeda$configs;
    @Unique private final Map<Module<?>, Supplier<ConfigHandler.Entry<Module.BaseConfig>>> andromeda$getters = new IdentityHashMap<>();

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ServerWorld;chunkManager:Lnet/minecraft/server/world/ServerChunkManager;", ordinal = 0, shift = At.Shift.AFTER), method = "<init>")
    private void andromeda$initStates(CallbackInfo ci) {
        var modules = ModuleManager.get().loaded().stream().filter(m -> Andromeda.getConfig(m).e.scope.isDimension()).toList();
        this.andromeda$configs = new ConfigHandler(getServer().session.getWorldDirectory(this.getRegistryKey()).resolve("world_config"), modules);
        this.andromeda$configs.setRoot(Andromeda.rootHandler());

        DataConfigs.get(this.getServer()).apply(this, this.getRegistryKey().getValue());
        ModuleManager.get().loaded().forEach(module -> andromeda$getters.put(module, ScopedConfigs.get(((ServerWorld) (Object) this), (Module<Module.BaseConfig>) module)));
    }

    @Override
    public <T extends Module.BaseConfig> ConfigHandler.Entry<T> am$get(Module<T> module) {
        var getter = andromeda$getters.get(module);
        if (getter == null) throw new IllegalStateException(module.meta().id());
        return (ConfigHandler.Entry<T>) getter.get();
    }

    @Override
    public ConfigHandler andromeda$getConfigs() {
        return andromeda$configs;
    }

    @Override
    public boolean am$isReady() {
        return true;
    }
}
