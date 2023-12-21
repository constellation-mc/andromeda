package me.melontini.andromeda.modules.blocks.campfire_effects.mixin;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.blocks.campfire_effects.CampfireEffects;
import me.melontini.andromeda.modules.blocks.campfire_effects.PotionUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CampfireBlockEntity.class)
class CampfireBlockEntityMixin {
    @Unique
    private static final CampfireEffects am$ceff = ModuleManager.quick(CampfireEffects.class);

    @Inject(at = @At("HEAD"), method = "litServerTick")
    private static void andromeda$litServerTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        if (world.getTime() % 180 == 0) {
                if (state.get(CampfireBlock.LIT)) {
                    var config = world.am$get(CampfireEffects.class);
                    if (!config.enabled) return;

                    List<LivingEntity> entities = new ArrayList<>();
                    world.getEntityLookup().forEachIntersects(new Box(pos).expand(config.effectsRange), entity -> {
                        if ((entity instanceof PassiveEntity && config.affectsPassive) || entity instanceof PlayerEntity) {
                            entities.add((LivingEntity) entity);
                        }
                    });
                    List<CampfireEffects.Config.Effect> effects = config.effectList;

                    for (LivingEntity player : entities) {
                        for (CampfireEffects.Config.Effect effect : effects) {
                            StatusEffectInstance effectInstance = new StatusEffectInstance(PotionUtil.getStatusEffect(Identifier.tryParse(effect.identifier)),
                                    200, effect.amplifier, true, false, true);
                            player.addStatusEffect(effectInstance);
                        }
                    }
                }
            }
    }
}
