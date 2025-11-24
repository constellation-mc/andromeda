package me.melontini.andromeda.common.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.NonNull;
import me.melontini.dark_matter.api.base.util.functions.Memoize;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
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
      LootContextParamSet type, Level world, BuilderFunction function) {
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
    return new LootContext.Builder(set).create(null);
  }

  public static List<ItemStack> prepareLoot(
      @NonNull Level world, @NonNull ResourceLocation lootId) {
    return ((ServerLevel) world)
        .getServer()
        .getLootData()
        .getLootTable(lootId)
        .getRandomItems(
            new LootParams.Builder(((ServerLevel) world)).create(LootContextParamSets.EMPTY));
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
    this.builder.withOptionalParameter(LootContextParams.KILLER_ENTITY, entity);
    return this;
  }

  public LootContextBuilder directKiller(Entity entity) {
    this.builder.withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, entity);
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
