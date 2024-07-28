package me.melontini.andromeda.common.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

public class LazyLootParameterSet extends LootContextParameterSet {

    private final Supplier<ServerWorld> world;
    private final Map<LootContextParameter<?>, Supplier<Object>> parameters;

    public LazyLootParameterSet(Supplier<ServerWorld> world, Map<LootContextParameter<?>, Supplier<Object>> parameters, Map<Identifier, DynamicDrop> dynamicDrops, float luck) {
        super(null, null, dynamicDrops, luck);
        this.world = world;
        this.parameters = parameters;
    }

    @Override
    public ServerWorld getWorld() {
        return world.get();
    }

    public <T> T get(LootContextParameter<T> parameter) {
        var object = this.parameters.get(parameter);
        if (object == null) {
            throw new NoSuchElementException(parameter.getId().toString());
        } else {
            return (T) object.get();
        }
    }

    @Nullable public <T> T method_51868(LootContextParameter<T> parameter) {
        var s = this.parameters.get(parameter);
        return s == null ? null : (T) s.get();
    }

    @Nullable public <T> T getOptional(LootContextParameter<T> parameter) {
        var s = this.parameters.get(parameter);
        return s == null ? null : (T) s.get();
    }

    public static class Builder {
        @Getter
        private final Supplier<ServerWorld> world;
        private final Map<LootContextParameter<?>, Supplier<Object>> parameters = Maps.newIdentityHashMap();
        private final Map<Identifier, DynamicDrop> dynamicDrops = Maps.newHashMap();
        private float luck;

        public Builder(Supplier<ServerWorld> world) {
            this.world = world;
        }

        public <T> Builder add(LootContextParameter<T> parameter, Supplier<T> value) {
            this.parameters.put(parameter, (Supplier<Object>) value);
            return this;
        }

        public Builder addDynamicDrop(Identifier id, DynamicDrop dynamicDrop) {
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

        public LootContextParameterSet build(LootContextType contextType) {
            Set<LootContextParameter<?>> set = Sets.difference(this.parameters.keySet(), contextType.getAllowed());
            if (!set.isEmpty()) {
                throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + set);
            } else {
                Set<LootContextParameter<?>> set2 = Sets.difference(contextType.getRequired(), this.parameters.keySet());
                if (!set2.isEmpty()) {
                    throw new IllegalArgumentException("Missing required parameters: " + set2);
                } else {
                    return new LazyLootParameterSet(this.world, this.parameters, this.dynamicDrops, this.luck);
                }
            }
        }
    }
}
