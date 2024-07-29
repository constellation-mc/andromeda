package me.melontini.andromeda.common.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RegistryLoader.class)
abstract class RegistryLoaderMixin {

  @WrapOperation(
      method =
          "load(Lnet/minecraft/registry/RegistryOps$RegistryInfoGetter;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/registry/MutableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lcom/mojang/serialization/Decoder;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;",
              remap = false))
  private static DataResult<?> andromeda$cancelDecode(
      Decoder<?> instance,
      DynamicOps<?> ops,
      Object input,
      Operation<DataResult<?>> original,
      @Local Identifier identifier,
      @Local JsonElement json) {
    // Only applying to Andromeda because this is very untested.
    if (identifier.getNamespace().equals("andromeda")
        && json.isJsonObject()
        && !ResourceConditions.objectMatchesConditions(json.getAsJsonObject())) return null;

    return original.call(instance, ops, input);
  }

  @WrapOperation(
      method =
          "load(Lnet/minecraft/registry/RegistryOps$RegistryInfoGetter;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/registry/MutableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lcom/mojang/serialization/DataResult;getOrThrow(ZLjava/util/function/Consumer;)Ljava/lang/Object;",
              remap = false))
  private static Object andromeda$cancelGetOrThrow(
      DataResult<?> instance,
      boolean allowPartial,
      Consumer<String> onError,
      Operation<?> original) {
    return instance == null ? null : original.call(instance, allowPartial, onError);
  }

  @WrapWithCondition(
      method =
          "load(Lnet/minecraft/registry/RegistryOps$RegistryInfoGetter;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/registry/MutableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/registry/MutableRegistry;add(Lnet/minecraft/registry/RegistryKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/registry/entry/RegistryEntry$Reference;"))
  private static boolean andromeda$cancelEntryAddition(
      MutableRegistry<?> instance, RegistryKey<?> tRegistryKey, Object t, Lifecycle lifecycle) {
    return t != null;
  }
}
