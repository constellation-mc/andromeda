package me.melontini.andromeda.common.mixin.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.common.data.DataPackContentsAccessor;
import me.melontini.andromeda.common.data.ServerResourceReloadersEvent;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mixin(DataPackContents.class)
abstract class DataPackContentsMixin implements DataPackContentsAccessor {

    @Shadow public abstract List<ResourceReloader> getContents();

    @Unique
    private Map<Identifier, IdentifiableResourceReloadListener> reloadersMap;
    @Unique
    private List<IdentifiableResourceReloadListener> reloaders;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void andromeda$addReloaders(DynamicRegistryManager.Immutable dynamicRegistryManager, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, int functionPermissionLevel, CallbackInfo ci) {
        List<IdentifiableResourceReloadListener> list = new ArrayList<>();
        ServerResourceReloadersEvent.EVENT.invoker().register(new ServerResourceReloadersEvent.Context(dynamicRegistryManager, enabledFeatures, environment, list::add));
        this.reloaders = list;

        var cls = IdentifiableResourceReloadListener.class;
        this.reloadersMap = getContents().stream().filter(cls::isInstance).map(cls::cast)
                .collect(ImmutableMap.toImmutableMap(IdentifiableResourceReloadListener::getFabricId, Function.identity()));
    }

    @Override
    public <T extends IdentifiableResourceReloadListener> T am$getReloader(Identifier identifier) {
        return (T) MakeSure.notNull(this.reloadersMap.get(identifier));
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "getContents")
    private List<ResourceReloader> andromeda$injectContents(List<ResourceReloader> original) {
        return Streams.concat(original.stream(), this.reloaders.stream()).distinct().toList();
    }
}
