package me.melontini.andromeda.common.data;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.util.Identifier;

public interface DataPackContentsAccessor {

    <T extends IdentifiableResourceReloadListener> T am$getReloader(Identifier identifier);
}
