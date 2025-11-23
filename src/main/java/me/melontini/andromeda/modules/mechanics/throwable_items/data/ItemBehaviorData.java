package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import me.melontini.andromeda.modules.mechanics.throwable_items.ItemBehavior;
import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.commander.api.command.Command;
import me.melontini.commander.api.event.EventContext;
import me.melontini.commander.api.event.EventKey;
import me.melontini.commander.api.event.EventType;
import me.melontini.commander.api.expression.Arithmetica;
import me.melontini.dark_matter.api.data.codecs.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ItemBehaviorData(Parameters parameters, List<Subscription> subscriptions)
    implements ItemBehavior {

  @Override
  public void onCollision(
          ItemStack stack,
          FlyingItemEntity fie,
          ServerLevel world,
          @Nullable Entity user,
          HitResult hitResult) {
    Stream<Subscription> stream = null;
    switch (hitResult.getType()) {
      case BLOCK -> stream = subscriptions.stream().filter(s -> s.event == Main.Event.BLOCK);
      case ENTITY -> stream = subscriptions.stream().filter(s -> s.event == Main.Event.ENTITY);
      case MISS -> stream = subscriptions.stream().filter(s -> s.event == Main.Event.MISS);
    }
    var list = Stream.concat(stream, subscriptions.stream().filter(s -> s.event == Main.Event.ANY))
        .toList();
    if (list.isEmpty()) return;

    LootParams.Builder builder = new LootParams.Builder(world);
    builder.withParameter(LootContextParams.DIRECT_KILLER_ENTITY, fie);
    builder.withOptionalParameter(LootContextParams.KILLER_ENTITY, user);
    builder.withParameter(LootContextParams.TOOL, stack);
    switch (hitResult.getType()) {
      case BLOCK -> {
        BlockHitResult result = (BlockHitResult) hitResult;
        builder.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(result.getBlockPos()));
        builder.withParameter(LootContextParams.BLOCK_STATE, world.getBlockState(result.getBlockPos()));
        builder.withOptionalParameter(
            LootContextParams.BLOCK_ENTITY, world.getBlockEntity(result.getBlockPos()));
      }
      case ENTITY -> {
        EntityHitResult result = (EntityHitResult) hitResult;
        builder.withParameter(LootContextParams.ORIGIN, result.getLocation());
        builder.withOptionalParameter(LootContextParams.THIS_ENTITY, result.getEntity());
      }
      case MISS -> builder.withParameter(LootContextParams.ORIGIN, hitResult.getLocation());
    }

    LootContext lootContext =
        new LootContext.Builder(builder.create(Main.CONTEXT_TYPE.orThrow())).create(null);
    EventContext context = EventContext.builder(EventType.NULL)
        .addParameter(EventKey.LOOT_CONTEXT, lootContext)
        .build();
    list.forEach(s -> s.commands().forEach(cc -> cc.execute(context)));
  }

  public record Subscription(Main.Event event, List<Command.Conditioned> commands) {
    public static final Codec<Subscription> CODEC = RecordCodecBuilder.create(data -> data.group(
            ExtraCodecs.enumCodec(Main.Event.class).fieldOf("event").forGetter(Subscription::event),
            ExtraCodecs.list(Command.CODEC.codec())
                .fieldOf("commands")
                .forGetter(Subscription::commands))
        .apply(data, Subscription::new));
    public static final Codec<List<Subscription>> LIST_CODEC = ExtraCodecs.list(CODEC);
  }

  public record Parameters(
      List<Item> items,
      boolean disabled,
      boolean override_vanilla,
      boolean complement,
      Arithmetica cooldown) {
    public static final MapCodec<Parameters> CODEC = RecordCodecBuilder.mapCodec(data -> data.group(
            ExtraCodecs.list(BuiltInRegistries.ITEM.byNameCodec())
                .fieldOf("items")
                .forGetter(Parameters::items),
            ExtraCodecs.optional("disabled", Codec.BOOL, false).forGetter(Parameters::disabled),
            ExtraCodecs.optional("override_vanilla", Codec.BOOL, false)
                .forGetter(Parameters::override_vanilla),
            ExtraCodecs.optional("complement", Codec.BOOL, true).forGetter(Parameters::complement),
            ExtraCodecs.optional("cooldown", Arithmetica.CODEC, Arithmetica.constant(50))
                .forGetter(Parameters::cooldown))
        .apply(data, Parameters::new));
  }

  public static final Codec<ItemBehaviorData> CODEC = new MapCodec<ItemBehaviorData>() {
    @Override
    public <T> RecordBuilder<T> encode(
        ItemBehaviorData input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
      Parameters.CODEC.encode(input.parameters(), ops, prefix);
      prefix.add("events", Subscription.LIST_CODEC.encodeStart(ops, input.subscriptions()));
      return prefix;
    }

    @Override
    public <T> DataResult<ItemBehaviorData> decode(DynamicOps<T> ops, MapLike<T> input) {
      return Parameters.CODEC
          .decode(ops, input)
          .flatMap(parameters1 -> {
            var subscriptions = input.get("events");
            if (subscriptions == null)
              return DataResult.error(() -> "Missing required 'events' field!");

            return Subscription.LIST_CODEC
                .parse(ops, subscriptions)
                .flatMap(subscriptions1 -> {
                  for (Subscription subscription : subscriptions1) {
                    for (Command.Conditioned command : subscription.commands) {
                      var r = command.validate(EventType.NULL);
                      if (r.error().isPresent()) return r.map(unused -> null);
                    }
                  }
                  return DataResult.success(subscriptions1);
                })
                .map(subscription1 -> new ItemBehaviorData(parameters1, subscription1));
          })
          .map(Function.identity());
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
      return Stream.of("events").map(ops::createString);
    }
  }.codec();

  public static ItemBehaviorData create(JsonObject object) {
    return CODEC.parse(JsonOps.INSTANCE, object).getOrThrow(false, string -> {
      throw new JsonParseException(string);
    });
  }
}
