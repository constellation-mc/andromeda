package me.melontini.andromeda.common.util.condition;

import com.mojang.serialization.MapCodec;
import me.melontini.andromeda.common.Andromeda;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ItemsRegisteredCondition(List<RegistryKey<Item>> keys) implements ResourceCondition {

    public static final MapCodec<ItemsRegisteredCondition> CODEC = RegistryKey.createCodec(Registries.ITEM.getKey()).listOf()
            .fieldOf("values").xmap(ItemsRegisteredCondition::new, ItemsRegisteredCondition::keys);
    public static final ResourceConditionType<ItemsRegisteredCondition> TYPE = ResourceConditionType.create(Andromeda.id("items_registered"), CODEC);

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(@Nullable RegistryWrapper.WrapperLookup registryLookup) {
        return keys.stream().allMatch(Registries.ITEM::contains);
    }
}
