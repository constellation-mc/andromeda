package me.melontini.andromeda.common.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.NonNull;
import me.melontini.dark_matter.api.base.util.functions.Memoize;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

// Wrapper for a LootContextParameterSet.Builder to add our utility methods.
public class LootContextBuilder {

  LootContextParameterSet.Builder builder;

  private LootContextBuilder(World world) {
    this.builder = new LootContextParameterSet.Builder((ServerWorld) world);
  }

  public static Supplier<LootContext> builder(
      LootContextType type, World world, BuilderFunction function) {
    return Memoize.supplier(
        () -> build(function.apply(new LootContextBuilder(world)).builder.build(type)));
  }

  public static Supplier<LootContext> block(World world, BuilderFunction function) {
    return builder(LootContextTypes.BLOCK, world, function);
  }

  public static Supplier<LootContext> fishing(World world, BuilderFunction function) {
    return builder(LootContextTypes.FISHING, world, function);
  }

  public static Supplier<LootContext> command(World world, BuilderFunction function) {
    return builder(LootContextTypes.COMMAND, world, function);
  }

  public static Supplier<LootContext> entity(World world, BuilderFunction function) {
    return builder(LootContextTypes.ENTITY, world, function);
  }

  public static LootContext build(LootContextParameterSet set) {
    return new LootContext.Builder(set).build(null);
  }

  public static List<ItemStack> prepareLoot(@NonNull World world, @NonNull Identifier lootId) {
    return ((ServerWorld) world)
        .getServer()
        .getLootManager()
        .getLootTable(lootId)
        .generateLoot(new LootContextParameterSet.Builder(((ServerWorld) world))
            .build(LootContextTypes.EMPTY));
  }

  public LootContextBuilder origin(BlockPos pos) {
    return this.origin(Vec3d.ofCenter(pos));
  }

  public LootContextBuilder origin(Entity entity) {
    return this.origin(entity.getPos());
  }

  public LootContextBuilder origin(Vec3d pos) {
    this.builder.addOptional(LootContextParameters.ORIGIN, pos);
    return this;
  }

  public LootContextBuilder state(BlockState state) {
    this.builder.addOptional(LootContextParameters.BLOCK_STATE, state);
    return this;
  }

  public LootContextBuilder tool(LivingEntity player, Hand hand) {
    return this.tool(player.getStackInHand(hand));
  }

  public LootContextBuilder tool(@Nullable ItemStack stack) {
    this.builder.addOptional(
        LootContextParameters.TOOL, Objects.requireNonNullElse(stack, ItemStack.EMPTY));
    return this;
  }

  public LootContextBuilder thisEntity(Entity entity) {
    this.builder.addOptional(LootContextParameters.THIS_ENTITY, entity);
    return this;
  }

  public LootContextBuilder killer(Entity entity) {
    this.builder.addOptional(LootContextParameters.KILLER_ENTITY, entity);
    return this;
  }

  public LootContextBuilder directKiller(Entity entity) {
    this.builder.addOptional(LootContextParameters.DIRECT_KILLER_ENTITY, entity);
    return this;
  }

  public LootContextBuilder blockEntity(BlockEntity entity) {
    this.builder.addOptional(LootContextParameters.BLOCK_ENTITY, entity);
    return this;
  }

  public LootContextBuilder source(DamageSource source) {
    this.builder.addOptional(LootContextParameters.DAMAGE_SOURCE, source);
    return this;
  }

  public LootContextBuilder genericSource() {
    return this.source(builder.getWorld().getDamageSources().generic());
  }

  public LootContextBuilder sourceOrGeneric(DamageSource source) {
    return source != null ? this.source(source) : this.genericSource();
  }

  public interface BuilderFunction {
    LootContextBuilder apply(LootContextBuilder builder);
  }
}
