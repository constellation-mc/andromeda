package me.melontini.andromeda.common.mixin;

import java.util.function.Supplier;
import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.common.util.LazyLootParameterSet;
import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.base.util.functions.Memoize;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
abstract class EntityMixin implements ConstantLootContextAccessor {

  @Shadow
  public abstract Vec3 position();

  @Shadow
  public abstract Level level();

  @Unique private final Supplier<LootContext> andromeda$context = Utilities.supply(() -> {
    var c = new LazyLootParameterSet.Builder(() -> (ServerLevel) level())
        .add(LootContextParams.ORIGIN, this::position)
        .add(LootContextParams.THIS_ENTITY, () -> (Entity) (Object) this)
        .build(LootContextParamSets.COMMAND);
    return Memoize.supplier(() -> LootContextBuilder.build(c));
  });

  @Override
  public Supplier<LootContext> andromeda$getLootContext() {
    return andromeda$context;
  }
}
