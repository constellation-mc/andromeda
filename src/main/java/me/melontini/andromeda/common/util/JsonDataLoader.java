package me.melontini.andromeda.common.util;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.util.Identifier;

public abstract class JsonDataLoader extends net.minecraft.resource.JsonDataLoader implements IdentifiableResourceReloadListener {

    protected final Gson gson;
    private final Identifier id;

    private JsonDataLoader(Gson gson, Identifier id) {
        super(gson, id.toString().replace(':', '/'));
        this.gson = gson;
        this.id = id;
    }

    protected JsonDataLoader(Identifier id) {
        this(new Gson(), id);
    }

    @Override
    public final Identifier getFabricId() {
        return this.id;
    }
}
