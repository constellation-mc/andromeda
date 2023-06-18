package me.melontini.andromeda.mixin.blocks.campfire_effects;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.InvalidConfigEntryException;
import me.melontini.andromeda.util.PotionUtil;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CampfireBlockEntity.class)
@MixinRelatedConfigOption("campfireTweaks.campfireEffects")
public class CampfireBlockEntityMixin {
    @Inject(at = @At("HEAD"), method = "litServerTick")
    private static void andromeda$litServerTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        if (Andromeda.CONFIG.campfireTweaks.campfireEffects) {
            if (world.getTime() % 180 == 0) {
                if (state.get(CampfireBlock.LIT)) {
                    List<LivingEntity> entities = new ArrayList<>();
                    world.getEntityLookup().forEachIntersects(new Box(pos).expand(Andromeda.CONFIG.campfireTweaks.campfireEffectsRange), entity -> {
                        if ((entity instanceof PassiveEntity && Andromeda.CONFIG.campfireTweaks.campfireEffectsPassive) || entity instanceof PlayerEntity) {
                            entities.add((LivingEntity) entity);
                        }
                    });
                    List<String> identifiers = Andromeda.CONFIG.campfireTweaks.campfireEffectsList;
                    List<Integer> amplifiers = Andromeda.CONFIG.campfireTweaks.campfireEffectsAmplifierList;

                    for (LivingEntity player : entities) {
                        if (identifiers.size() == amplifiers.size()) {
                            for (int i = 0; i < identifiers.size(); i++) {
                                StatusEffectInstance effectInstance = new StatusEffectInstance(PotionUtil.getStatusEffect(Identifier.tryParse(identifiers.get(i))),
                                        200, amplifiers.get(i), true, false, true);
                                player.addStatusEffect(effectInstance);
                            }
                        } else {
                            throw new InvalidConfigEntryException(String.format("campfireEffectsList (size: %s) & campfireEffectsAmplifierList (size: %s) don't match in size!", identifiers.size(), amplifiers.size()));
                        }
                    }
                }
            }
        }
    }
}
