package dev.zenfyr.andromeda.modules.blocks.campfire_effects.mixin;

import java.util.ArrayList;
import java.util.List;
import dev.zenfyr.andromeda.modules.blocks.campfire_effects.CampfireEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
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
      if (state.getValue(CampfireBlock.LIT)) {
        var config = world.am$get(CampfireEffects.CONFIG);
        if (!config.available) return;

        List<LivingEntity> entities = new ArrayList<>();
        double rad = config.effectsRange;
        boolean affectsPassive = config.affectsPassive;
        world.getEntities().get(new AABB(pos).inflate(rad), entity -> {
          if ((entity instanceof AgeableMob && affectsPassive) || entity instanceof Player) {
            entities.add((LivingEntity) entity);
          }
        });
        List<CampfireEffects.Config.Effect> effects = config.effectList;

        for (LivingEntity player : entities) {
          for (CampfireEffects.Config.Effect effect : effects) {
            MobEffectInstance effectInstance =
                new MobEffectInstance(effect.identifier, 200, effect.amplifier, true, false, true);
            player.addEffect(effectInstance);
          }
        }
      }
    }
  }
}
