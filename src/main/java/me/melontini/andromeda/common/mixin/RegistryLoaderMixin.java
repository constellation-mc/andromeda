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
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RegistryDataLoader.class)
abstract class RegistryLoaderMixin {

  @WrapOperation(
      method =
          "loadRegistryContents(Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/WritableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
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
      @Local ResourceLocation identifier,
      @Local JsonElement json) {
    // Only applying to Andromeda because this is very untested.
    if (identifier.getNamespace().equals("andromeda")
        && json.isJsonObject()
        && !ResourceConditions.objectMatchesConditions(json.getAsJsonObject())) return null;

    return original.call(instance, ops, input);
  }

  @WrapOperation(
      method =
          "loadRegistryContents(Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/WritableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
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
          "loadRegistryContents(Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/WritableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/core/WritableRegistry;register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/Holder$Reference;"))
  private static boolean andromeda$cancelEntryAddition(
      WritableRegistry<?> instance, ResourceKey<?> tRegistryKey, Object t, Lifecycle lifecycle) {
    return t != null;
  }
}
