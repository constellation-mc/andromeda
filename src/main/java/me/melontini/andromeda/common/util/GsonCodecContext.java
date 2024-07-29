package me.melontini.andromeda.common.util;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Type;

public record GsonCodecContext<C>(Codec<C> codec)
    implements JsonSerializer<C>, JsonDeserializer<C> {

  public static <C> GsonCodecContext<C> of(Codec<C> codec) {
    return new GsonCodecContext<>(codec);
  }

  @Override
  public C deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    var r = this.codec.parse(JsonOps.INSTANCE, json);
    if (r.error().isPresent())
      throw new JsonParseException(r.error().orElseThrow().message());
    return r.result().orElseThrow();
  }

  @Override
  public JsonElement serialize(C src, Type typeOfSrc, JsonSerializationContext context) {
    var r = codec.encodeStart(JsonOps.INSTANCE, src);
    if (r.error().isPresent())
      throw new IllegalStateException(r.error().orElseThrow().message());
    return r.result().orElseThrow();
  }
}
