package dev.zenfyr.andromeda.modules.blocks.leaf_slowdown.mixin;

import dev.zenfyr.andromeda.modules.blocks.leaf_slowdown.LeafSlowdown;
import dev.zenfyr.andromeda.modules.blocks.leaf_slowdown.LeafSlowdownMain;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {

  public LivingEntityMixin(EntityType<?> type, Level level) {
    super(type, level);
  }

  @Inject(at = @At("TAIL"), method = "baseTick")
  public void andromeda$tick(CallbackInfo ci) {
    if (this.level().isClientSide()) return;
    if (!this.level().am$get(LeafSlowdown.CONFIG).available) return;
    LivingEntity self = (LivingEntity) (Object) this;

    boolean isCreative = self instanceof Player player && player.getAbilities().mayfly;
    if (isCreative) return;

    AttributeInstance attribute = self.getAttribute(Attributes.MOVEMENT_SPEED);
    if (attribute == null) return;

    var below = this.level().getBlockState(blockPosition().below());
    var x2Below = this.level().getBlockState(blockPosition().below(2));
    boolean isLeaves =
        below.is(BlockTags.LEAVES) || (x2Below.is(BlockTags.LEAVES) && below.isAir());
    if (!isLeaves) {
      if (attribute.hasModifier(LeafSlowdownMain.LEAF_SLOWNESS_LOCATION))
        attribute.removeModifier(LeafSlowdownMain.LEAF_SLOWNESS);
      return;
    }

    if (!attribute.hasModifier(LeafSlowdownMain.LEAF_SLOWNESS_LOCATION))
      attribute.addTransientModifier(LeafSlowdownMain.LEAF_SLOWNESS);
  }
}
