package me.melontini.andromeda.modules.blocks.campfire_effects.mixin;

import com.google.common.base.Suppliers;
import me.melontini.andromeda.modules.blocks.campfire_effects.CampfireEffects;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.loot.context.LootContextParameters.*;

@Mixin(CampfireBlockEntity.class)
abstract class CampfireBlockEntityMixin {

    @Inject(at = @At("HEAD"), method = "litServerTick")
    private static void andromeda$litServerTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        if (world.getTime() % 180 == 0) {
                if (state.get(CampfireBlock.LIT)) {
                    var config = world.am$get(CampfireEffects.class);
                    if (!config.e.enabled) return;

                    List<LivingEntity> entities = new ArrayList<>();
                    var supplier = Suppliers.memoize(() -> {
                        LootContextParameterSet set = new LootContextParameterSet.Builder((ServerWorld) world)
                                .add(ORIGIN, Vec3d.ofCenter(pos))
                                .add(BLOCK_STATE, state)
                                .add(BLOCK_ENTITY, campfire)
                                .add(TOOL, ItemStack.EMPTY)
                                .build(LootContextTypes.BLOCK);

                        return new LootContext.Builder(set).build(null);
                    });
                    double rad = config.c.effectsRange.asDouble(supplier);
                    world.getEntityLookup().forEachIntersects(new Box(pos).expand(rad), entity -> {
                        if ((entity instanceof PassiveEntity && config.c.affectsPassive) || entity instanceof PlayerEntity) {
                            entities.add((LivingEntity) entity);
                        }
                    });
                    List<CampfireEffects.Config.Effect> effects = config.c.effectList;

                    for (LivingEntity player : entities) {
                        for (CampfireEffects.Config.Effect effect : effects) {
                            StatusEffectInstance effectInstance = new StatusEffectInstance(effect.identifier,
                                    200, effect.amplifier.asInt(supplier), true, false, true);
                            player.addStatusEffect(effectInstance);
                        }
                    }
                }
            }
    }
}
