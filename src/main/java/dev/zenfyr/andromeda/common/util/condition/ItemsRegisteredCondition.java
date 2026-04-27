package dev.zenfyr.andromeda.common.util.condition;

import com.mojang.serialization.MapCodec;
import dev.zenfyr.andromeda.common.Andromeda;
import java.util.List;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public record ItemsRegisteredCondition(List<ResourceKey<Item>> keys) implements ResourceCondition {

  public static final MapCodec<ItemsRegisteredCondition> CODEC = ResourceKey.codec(
          BuiltInRegistries.ITEM.key())
      .listOf()
      .fieldOf("values")
      .xmap(ItemsRegisteredCondition::new, ItemsRegisteredCondition::keys);
  public static final ResourceConditionType<ItemsRegisteredCondition> TYPE =
      ResourceConditionType.create(Andromeda.id("items_registered"), CODEC);

  @Override
  public ResourceConditionType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryLookup) {
    return keys.stream().allMatch(BuiltInRegistries.ITEM::containsKey);
  }
}
