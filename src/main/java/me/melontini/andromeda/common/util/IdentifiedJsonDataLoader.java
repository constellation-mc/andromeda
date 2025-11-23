package me.melontini.andromeda.common.util;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public abstract class IdentifiedJsonDataLoader extends net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
    implements IdentifiableResourceReloadListener {

  protected final Gson gson;
  private final ResourceLocation id;

  private IdentifiedJsonDataLoader(Gson gson, ResourceLocation id) {
    super(gson, id.toString().replace(':', '/'));
    this.gson = gson;
    this.id = id;
  }

  protected IdentifiedJsonDataLoader(ResourceLocation id) {
    this(new Gson(), id);
  }

  @Override
  public final ResourceLocation getFabricId() {
    return this.id;
  }
}
