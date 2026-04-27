package dev.zenfyr.andromeda.common.util.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import java.util.List;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;

public record ModulesLoadedCondition(List<String> modules) implements ResourceCondition {

  public static final MapCodec<ModulesLoadedCondition> CODEC = Codec.STRING
      .listOf()
      .fieldOf("values")
      .xmap(ModulesLoadedCondition::new, ModulesLoadedCondition::modules);
  public static final ResourceConditionType<ModulesLoadedCondition> TYPE =
      ResourceConditionType.create(Andromeda.id("modules_loaded"), CODEC);

  @Override
  public ResourceConditionType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryInfo) {
    return modules.stream().allMatch(string -> ModuleManager.get().get(string).isPresent());
  }
}
