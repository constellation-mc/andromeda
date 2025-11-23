package me.melontini.andromeda.common.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import org.jetbrains.annotations.Nullable;

public class LazyLootParameterSet extends LootParams {

  private final Supplier<ServerLevel> world;
  private final Map<LootContextParam<?>, Supplier<Object>> parameters;

  public LazyLootParameterSet(
      Supplier<ServerLevel> world,
      Map<LootContextParam<?>, Supplier<Object>> parameters,
      Map<ResourceLocation, DynamicDrop> dynamicDrops,
      float luck) {
    super(null, null, dynamicDrops, luck);
    this.world = world;
    this.parameters = parameters;
  }

  @Override
  public ServerLevel getLevel() {
    return world.get();
  }

  public <T> T getParameter(LootContextParam<T> parameter) {
    var object = this.parameters.get(parameter);
    if (object == null) {
      throw new NoSuchElementException(parameter.getName().toString());
    } else {
      return (T) object.get();
    }
  }

  @Nullable public <T> T getOptionalParameter(LootContextParam<T> parameter) {
    var s = this.parameters.get(parameter);
    return s == null ? null : (T) s.get();
  }

  @Nullable public <T> T getParamOrNull(LootContextParam<T> parameter) {
    var s = this.parameters.get(parameter);
    return s == null ? null : (T) s.get();
  }

  public static class Builder {
    @Getter
    private final Supplier<ServerLevel> world;

    private final Map<LootContextParam<?>, Supplier<Object>> parameters = Maps.newIdentityHashMap();
    private final Map<ResourceLocation, DynamicDrop> dynamicDrops = Maps.newHashMap();
    private float luck;

    public Builder(Supplier<ServerLevel> world) {
      this.world = world;
    }

    public <T> Builder add(LootContextParam<T> parameter, Supplier<T> value) {
      this.parameters.put(parameter, (Supplier<Object>) value);
      return this;
    }

    public Builder addDynamicDrop(ResourceLocation id, DynamicDrop dynamicDrop) {
      DynamicDrop dynamicDrop2 = this.dynamicDrops.put(id, dynamicDrop);
      if (dynamicDrop2 != null) {
        throw new IllegalStateException("Duplicated dynamic drop '" + this.dynamicDrops + "'");
      } else {
        return this;
      }
    }

    public Builder luck(float luck) {
      this.luck = luck;
      return this;
    }

    public LootParams build(LootContextParamSet contextType) {
      Set<LootContextParam<?>> set =
          Sets.difference(this.parameters.keySet(), contextType.getAllowed());
      if (!set.isEmpty()) {
        throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + set);
      } else {
        Set<LootContextParam<?>> set2 =
            Sets.difference(contextType.getRequired(), this.parameters.keySet());
        if (!set2.isEmpty()) {
          throw new IllegalArgumentException("Missing required parameters: " + set2);
        } else {
          return new LazyLootParameterSet(
              this.world, this.parameters, this.dynamicDrops, this.luck);
        }
      }
    }
  }
}
