package me.melontini.andromeda.common.client.config;

import me.melontini.andromeda.base.Module;
import me.melontini.dark_matter.api.base.util.classes.Tuple;
import net.minecraft.text.Text;

import java.util.*;
import java.util.function.BooleanSupplier;

public class FeatureBlockade {

    private static final FeatureBlockade INSTANCE = new FeatureBlockade();

    private final Map<Module<?>, Map<String, Tuple<Set<Text>, BooleanSupplier>>> blockades = new IdentityHashMap<>();

    public static FeatureBlockade get() {
        return INSTANCE;
    }

    public FeatureBlockade explain(Module<?> module, String feature, BooleanSupplier predicate, Text explanation) {
        blockades.computeIfAbsent(module, m -> new HashMap<>()).computeIfAbsent(feature, f -> Tuple.of(new HashSet<>(), predicate)).left().add(explanation);
        return this;
    }


    public Optional<Set<Text>> explain(Module<?> module, String feature) {
        return Optional.ofNullable(blockades.get(module)).map(m -> {
            var s = m.get(feature);
            if (s != null && s.right().getAsBoolean()) return s.left();
            return null;
        });
    }
}
