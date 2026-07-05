package dev.zenfyr.andromeda.modules.blocks.leaf_slowdown.mixin;

import dev.zenfyr.andromeda.modules.blocks.leaf_slowdown.LeafSlowdown;
import dev.zenfyr.andromeda.modules.blocks.leaf_slowdown.Main;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class EntityMixin extends Entity {

  @Shadow
  public abstract @Nullable AttributeInstance getAttribute(Holder<Attribute> attribute);

  public EntityMixin(EntityType<?> type, Level level) {
    super(type, level);
  }

  @Inject(at = @At("HEAD"), method = "baseTick")
  public void andromeda$tick(CallbackInfo ci) {
    if (!this.level().isClientSide() && this.level().am$get(LeafSlowdown.CONFIG).available) {
      AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
      if (this.level().getBlockState(blockPosition().below()).is(BlockTags.LEAVES)
          || (this.level().getBlockState(blockPosition().below(2)).is(BlockTags.LEAVES)
              && this.level().getBlockState(blockPosition().below()).is(Blocks.AIR))) {
        if (((LivingEntity) (Object) this) instanceof Player player
            && (player.isCreative() || player.isSpectator())) return;
        if (attributeInstance != null)
          if (!attributeInstance.hasModifier(Main.LEAF_SLOWNESS_LOCATION)) {
            attributeInstance.addTransientModifier(Main.LEAF_SLOWNESS);
          }
        /*Does this even work?*/
        setDeltaMovement(
            getDeltaMovement().x(),
            getDeltaMovement().y() * 0.7,
            getDeltaMovement().z());
      } else {
        if (attributeInstance != null)
          if (attributeInstance.hasModifier(Main.LEAF_SLOWNESS_LOCATION)) {
            attributeInstance.removeModifier(Main.LEAF_SLOWNESS_LOCATION);
          }
      }
    }
  }
}
