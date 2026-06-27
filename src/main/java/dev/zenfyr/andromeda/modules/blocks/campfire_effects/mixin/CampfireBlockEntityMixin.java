package dev.zenfyr.andromeda.modules.blocks.campfire_effects.mixin;

import dev.zenfyr.andromeda.modules.blocks.campfire_effects.CampfireEffects;
import dev.zenfyr.andromeda.modules.blocks.campfire_effects.CampfireEffectsData;
import dev.zenfyr.pulsar.api.loot.LootContextBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
abstract class CampfireBlockEntityMixin {

  @Inject(at = @At("HEAD"), method = "cookTick")
  private static void andromeda$litServerTick(
      Level world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
    if (world.getGameTime() % 180 == 0) {
      var config = world.am$get(CampfireEffects.CONFIG);
      if (!config.available) return;
      var data = world.getServer().pulsar$getReloadListener(CampfireEffectsData.RELOADER);
      if (data == null) return;
      var campfireEffect = data.get(state.getBlockHolder());
      if (campfireEffect == null) return;

      boolean accept = campfireEffect
          .condition()
          .map(condition -> condition.test(LootContextBuilder.block(
              world,
              builder ->
                  builder.origin(pos).state(state).tool(ItemStack.EMPTY).blockEntity(campfire))))
          .orElse(true);
      if (!accept) return;

      List<LivingEntity> entities = new ArrayList<>();
      world.getEntities(null, new AABB(pos).inflate(campfireEffect.range())).forEach(entity -> {
        if ((entity instanceof AgeableMob && campfireEffect.affectsPassive())
            || entity instanceof Player) {
          entities.add((LivingEntity) entity);
        }
      });
      var campfireEffects = campfireEffect.effects();

      for (LivingEntity player : entities) {
        for (var effect : campfireEffects) {
          boolean apply = effect
              .condition()
              .map(condition -> condition.test(LootContextBuilder.block(
                  world,
                  builder -> builder
                      .origin(pos)
                      .state(state)
                      .tool(ItemStack.EMPTY)
                      .thisEntity(player)
                      .blockEntity(campfire))))
              .orElse(true);

          if (apply) {
            MobEffectInstance effectInstance = new MobEffectInstance(
                effect.effect().value(), 200, effect.amplifier(), true, false, true);
            player.addEffect(effectInstance);
          }
        }
      }
    }
  }
}
