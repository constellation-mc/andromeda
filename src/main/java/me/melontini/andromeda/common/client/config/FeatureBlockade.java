package me.melontini.andromeda.common.client.config;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.ToBooleanFunction;
import me.melontini.dark_matter.api.base.util.tuple.Tuple;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.text.Text;

import java.util.*;

public final class FeatureBlockade {

    private static final FeatureBlockade INSTANCE = new FeatureBlockade();

    private final IdentityHashMap<Module, Map<String, Tuple<Set<Text>, ToBooleanFunction<ModuleManager>>>> blockades = new IdentityHashMap<>();

    public static synchronized FeatureBlockade get() {
        return INSTANCE;
    }

    private FeatureBlockade() {}

    public FeatureBlockade explain(Module module, String feature, ToBooleanFunction<ModuleManager> predicate, String key) {
        blockades.computeIfAbsent(module, m -> new HashMap<>()).computeIfAbsent(feature, f -> Tuple.of(new HashSet<>(), predicate)).left().add(TextUtil.translatable(key));
        return this;
    }

    public Optional<Set<Text>> explain(ModuleManager manager, Module module, String feature) {
        return Optional.ofNullable(blockades.get(module)).map(m -> {
            var s = m.get(feature);
            if (s != null && s.right().getAsBoolean(manager)) return s.left();
            return null;
        });
    }

    public String andromeda(String key) {
        return "andromeda.config.option_manager.reason.andromeda." + key;
    }
}
