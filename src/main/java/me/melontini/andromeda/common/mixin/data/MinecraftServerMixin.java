package me.melontini.andromeda.common.mixin.data;

import me.melontini.andromeda.common.data.DataPackContentsAccessor;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin implements DataPackContentsAccessor {

    @Shadow private MinecraftServer.ResourceManagerHolder resourceManagerHolder;

    @Override
    public <T extends IdentifiableResourceReloadListener> T am$getReloader(Identifier identifier) {
        return ((DataPackContentsAccessor)this.resourceManagerHolder.dataPackContents()).am$getReloader(identifier);
    }
}
