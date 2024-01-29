package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.melontini.andromeda.common.util.JsonDataLoader;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static me.melontini.andromeda.common.registries.Common.id;

public class BehaviorLoader extends JsonDataLoader {

    public BehaviorLoader() {
        super(new Gson(), "andromeda/item_throw_behaviors");
    }

    @Override
    public Identifier getFabricId() {
        return id("item_throw_behaviors");
    }

    @Override
    public CompletableFuture<Void> apply(Map<Identifier, JsonObject> data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, ItemBehaviorData> map = new HashMap<>();
            data.forEach((identifier, object) -> map.put(identifier, ItemBehaviorData.create(object)));
            return map;
        }, executor).thenAcceptAsync(map -> {
            ItemBehaviorManager.clear();
            EntrypointRunner.runEntrypoint("andromeda:collect_behaviors", Runnable.class, Runnable::run);

            map.forEach((id, behaviorData) -> {
                if (behaviorData.items().isEmpty()) return;

                for (Item item : behaviorData.items()) {
                    if (behaviorData.disabled()) {
                        ItemBehaviorManager.disable(item);
                        continue;
                    }

                    ItemBehaviorManager.addBehavior(item, ItemBehaviorAdder.dataPack(behaviorData), behaviorData.complement());
                    if (behaviorData.override_vanilla()) ItemBehaviorManager.overrideVanilla(item);

                    if (behaviorData.cooldown_time() != 50) {
                        ItemBehaviorManager.addCustomCooldown(item, behaviorData.cooldown_time());
                    }
                }
            });
        }, executor);
    }
}
