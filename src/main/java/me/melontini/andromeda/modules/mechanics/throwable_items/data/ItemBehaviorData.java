package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import me.melontini.andromeda.modules.mechanics.throwable_items.ItemBehavior;
import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.Event;
import me.melontini.dark_matter.api.minecraft.data.ExtraCodecs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record ItemBehaviorData(List<Item> items, boolean disabled, boolean override_vanilla, boolean complement,
                               Optional<Integer> cooldown, List<Event> events) implements ItemBehavior {

    public static final Codec<ItemBehaviorData> CODEC = RecordCodecBuilder.create(data -> data.group(
            ExtraCodecs.list(CommonRegistries.items().getCodec()).fieldOf("items").forGetter(ItemBehaviorData::items),

            Codec.BOOL.optionalFieldOf("disabled", false).forGetter(ItemBehaviorData::disabled),
            Codec.BOOL.optionalFieldOf("override_vanilla", false).forGetter(ItemBehaviorData::override_vanilla),
            Codec.BOOL.optionalFieldOf("complement", true).forGetter(ItemBehaviorData::complement),
            Codec.INT.optionalFieldOf("cooldown").forGetter(ItemBehaviorData::cooldown),

            ExtraCodecs.list(Event.CODEC).optionalFieldOf("events", Collections.emptyList()).forGetter(ItemBehaviorData::events)
    ).apply(data, ItemBehaviorData::new));

    @Override
    public void onCollision(ItemStack stack, FlyingItemEntity fie, ServerWorld world, @Nullable Entity user, HitResult hitResult) {
        LootContextParameterSet.Builder set = new LootContextParameterSet.Builder(world);
        if (user != null) set.add(LootContextParameters.KILLER_ENTITY, user);
        set.add(LootContextParameters.DIRECT_KILLER_ENTITY, fie);
        set.add(LootContextParameters.TOOL, stack);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            set.add(LootContextParameters.BLOCK_STATE, world.getBlockState(blockHitResult.getBlockPos()));
            BlockEntity blockEntity = world.getBlockEntity(blockHitResult.getBlockPos());
            if (blockEntity != null) set.add(LootContextParameters.BLOCK_ENTITY, blockEntity);
        }

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            set.add(LootContextParameters.THIS_ENTITY, entityHitResult.getEntity());
        }

        LootContext context = new LootContext.Builder(set.build(Main.ITEM_CONTEXT.orThrow())).build(null);
        Context context1 = new Context(stack, fie, world, user, hitResult, context);
        this.events().stream().filter(event -> event.canRun(hitResult)).forEach(event -> event.onCollision(context1));
    }
}
