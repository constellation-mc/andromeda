package me.melontini.andromeda.common.util.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.Andromeda;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ModulesLoadedCondition(List<String> modules) implements ResourceCondition {

    public static final MapCodec<ModulesLoadedCondition> CODEC = Codec.STRING.listOf()
            .fieldOf("values").xmap(ModulesLoadedCondition::new, ModulesLoadedCondition::modules);
    public static final ResourceConditionType<ModulesLoadedCondition> TYPE = ResourceConditionType.create(Andromeda.id("modules_loaded"), CODEC);

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(@Nullable RegistryWrapper.WrapperLookup registryLookup) {
        return modules.stream().allMatch(string -> ModuleManager.get().getModule(string).isPresent());
    }
}
