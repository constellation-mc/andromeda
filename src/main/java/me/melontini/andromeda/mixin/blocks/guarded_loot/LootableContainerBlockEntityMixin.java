package me.melontini.andromeda.mixin.blocks.guarded_loot;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(LootableContainerBlockEntity.class)
@MixinRelatedConfigOption("guardedLoot.enabled")
public abstract class LootableContainerBlockEntityMixin extends LockableContainerBlockEntity {

    protected LootableContainerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"), method = "checkUnlocked")
    private boolean lockedIfMonstersNearby(boolean locked, @Local PlayerEntity player) {
        if (Config.get().guardedLoot.enabled) {
            List<LivingEntity> monster = player.world.getEntitiesByClass(LivingEntity.class, new Box(this.getPos()).expand(Config.get().guardedLoot.range), Entity::isAlive).stream().filter(livingEntity -> livingEntity instanceof Monster)
                    .toList();

            if (!monster.isEmpty()) {
                player.sendMessage(TextUtil.translatable("andromeda.container.guarded").formatted(Formatting.RED), true);
                player.playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                player.emitGameEvent(GameEvent.CONTAINER_OPEN);

                for (LivingEntity livingEntity : monster) {
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 5 * 20, 0, false, false));
                }
                return true;
            }
        }
        return locked;
    }
}
