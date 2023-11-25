package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.google.gson.JsonObject;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import me.melontini.dark_matter.api.base.util.classes.Tuple;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.item.Item;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import static me.melontini.andromeda.registries.Common.id;

public class BehaviorLoader implements IdentifiableResourceReloadListener {

    @Override
    public Identifier getFabricId() {
        return id("item_throw_behaviors");
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        ItemBehaviorManager.clear();
        EntrypointRunner.runEntrypoint("andromeda:collect_behaviors", Runnable.class, Runnable::run);

        return CompletableFuture.supplyAsync(() -> manager.findResources("andromeda/item_throw_behaviors", identifier -> identifier.getPath().endsWith(".json")), prepareExecutor)
                .thenComposeAsync(behaviors -> {
                    Map<Identifier, CompletableFuture<Tuple<Set<Item>, ItemBehaviorData>>> map = new HashMap<>();

                    for (Map.Entry<Identifier, Resource> entry : behaviors.entrySet()) {
                        Identifier identifier = entry.getKey();
                        String string = identifier.getPath();
                        Identifier identifier2 = new Identifier(identifier.getNamespace(), string.substring("andromeda/item_throw_behavior/".length(), string.length() - ".json".length()));

                        map.put(identifier2, CompletableFuture.supplyAsync(() -> {
                            try (InputStream stream = entry.getValue().getInputStream(); Reader reader = new InputStreamReader(stream)) {
                                JsonObject object = JsonHelper.deserialize(reader);
                                if (!ResourceConditions.objectMatchesConditions(object))
                                    return Tuple.of(Collections.emptySet(), ItemBehaviorData.DEFAULT);

                                return ItemBehaviorData.create(object);
                            } catch (IOException e) {
                                throw new CompletionException(e);
                            }
                        }, prepareExecutor));
                    }

                    CompletableFuture<?>[] futures = map.values().toArray(CompletableFuture[]::new);
                    return CompletableFuture.allOf(futures).handle((unused, throwable) -> map);
                }).thenCompose(synchronizer::whenPrepared).thenAcceptAsync(map -> map.forEach((identifier, future) -> future.handle((tuple, throwable) -> {
                    for (Item item : tuple.left()) {
                        ItemBehaviorManager.addBehavior(item, ItemBehaviorAdder.dataPack(tuple.right()), tuple.right().complement);
                        if (tuple.right().override_vanilla) ItemBehaviorManager.overrideVanilla(item);

                        if (tuple.right().cooldown_time != 50) {
                            ItemBehaviorManager.addCustomCooldown(item, tuple.right().cooldown_time);
                        }
                    }
                    return null;
                }).join()), applyExecutor);
    }
}
