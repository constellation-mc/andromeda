package dev.zenfyr.andromeda.common.util;

import dev.zenfyr.pulsar.api.util.functions.Memoize;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.NonNull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// Wrapper for a LootContextParameterSet.Builder to add our utility methods.
public class LootContextBuilder {

  LootParams.Builder builder;

  private LootContextBuilder(Level world) {
    this.builder = new LootParams.Builder((ServerLevel) world);
  }

  public static Supplier<LootContext> builder(
      ContextKeySet type, Level world, BuilderFunction function) {
    return Memoize.supplier(
        () -> build(function.apply(new LootContextBuilder(world)).builder.create(type)));
  }

  public static Supplier<LootContext> block(Level world, BuilderFunction function) {
    return builder(LootContextParamSets.BLOCK, world, function);
  }

  public static Supplier<LootContext> fishing(Level world, BuilderFunction function) {
    return builder(LootContextParamSets.FISHING, world, function);
  }

  public static Supplier<LootContext> command(Level world, BuilderFunction function) {
    return builder(LootContextParamSets.COMMAND, world, function);
  }

  public static Supplier<LootContext> entity(Level world, BuilderFunction function) {
    return builder(LootContextParamSets.ENTITY, world, function);
  }

  public static LootContext build(LootParams set) {
    return new LootContext.Builder(set).create(Optional.empty());
  }

  public static List<ItemStack> prepareLoot(
      @NonNull Level world, @NonNull ResourceKey<LootTable> lootId) {
    return world
        .getServer()
        .reloadableRegistries()
        .lookup()
        .lookup(Registries.LOOT_TABLE)
        .flatMap(reg -> reg.get(lootId))
        .map(Holder.Reference::value)
        .<List<ItemStack>>map(loot -> loot.getRandomItems(
            new LootParams.Builder(((ServerLevel) world)).create(LootContextParamSets.EMPTY)))
        .orElse(List.of());
  }

  public LootContextBuilder origin(BlockPos pos) {
    return this.origin(Vec3.atCenterOf(pos));
  }

  public LootContextBuilder origin(Entity entity) {
    return this.origin(entity.position());
  }

  public LootContextBuilder origin(Vec3 pos) {
    this.builder.withOptionalParameter(LootContextParams.ORIGIN, pos);
    return this;
  }

  public LootContextBuilder state(BlockState state) {
    this.builder.withOptionalParameter(LootContextParams.BLOCK_STATE, state);
    return this;
  }

  public LootContextBuilder tool(LivingEntity player, InteractionHand hand) {
    return this.tool(player.getItemInHand(hand));
  }

  public LootContextBuilder tool(@Nullable ItemStack stack) {
    this.builder.withOptionalParameter(
        LootContextParams.TOOL, Objects.requireNonNullElse(stack, ItemStack.EMPTY));
    return this;
  }

  public LootContextBuilder thisEntity(Entity entity) {
    this.builder.withOptionalParameter(LootContextParams.THIS_ENTITY, entity);
    return this;
  }

  public LootContextBuilder killer(Entity entity) {
    this.builder.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, entity);
    return this;
  }

  public LootContextBuilder directKiller(Entity entity) {
    this.builder.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, entity);
    return this;
  }

  public LootContextBuilder blockEntity(BlockEntity entity) {
    this.builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, entity);
    return this;
  }

  public LootContextBuilder source(DamageSource source) {
    this.builder.withOptionalParameter(LootContextParams.DAMAGE_SOURCE, source);
    return this;
  }

  public LootContextBuilder genericSource() {
    return this.source(builder.getLevel().damageSources().generic());
  }

  public LootContextBuilder sourceOrGeneric(DamageSource source) {
    return source != null ? this.source(source) : this.genericSource();
  }

  public interface BuilderFunction {
    LootContextBuilder apply(LootContextBuilder builder);
  }
}
