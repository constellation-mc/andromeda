package dev.zenfyr.andromeda.modules.mechanics.throwable_items.mixin;

import static dev.zenfyr.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager.RELOADER;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.zenfyr.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import dev.zenfyr.andromeda.modules.mechanics.throwable_items.ItemBehavior;
import dev.zenfyr.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

  @Shadow
  public abstract Item getItem();

  @Shadow
  public abstract void shrink(int amount);

  @Inject(at = @At("HEAD"), method = "use", cancellable = true)
  private void andromeda$throwableBehavior(
      Level world,
      Player user,
      InteractionHand hand,
      CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
    if (world.isClientSide()) return;
    ItemStack stack = (ItemStack) (Object) this;

    var manager = world.getServer().dm$getReloader(RELOADER);
    if (manager.hasBehaviors(stack) && manager.overridesVanilla(getItem())) {
      if (andromeda$runBehaviors(world, manager, user)) {
        cir.setReturnValue(InteractionResultHolder.success(stack));
      }
    }
  }

  @ModifyReturnValue(at = @At("RETURN"), method = "use")
  private InteractionResultHolder<ItemStack> andromeda$throwableBehavior(
      InteractionResultHolder<ItemStack> original, Level world, Player user, InteractionHand hand) {
    if (world.isClientSide()) return original;
    ItemStack stack = (ItemStack) (Object) this;

    var manager = world.getServer().dm$getReloader(RELOADER);
    if (original.getResult() == InteractionResult.PASS
        && manager.hasBehaviors(stack)
        && !manager.overridesVanilla(getItem())) {
      if (andromeda$runBehaviors(world, manager, user)) {
        return InteractionResultHolder.success(stack);
      }
    }
    return original;
  }

  @Unique private boolean andromeda$runBehaviors(Level world, ItemBehaviorManager manager, Player user) {
    world.playSound(
        null,
        user.getX(),
        user.getY(),
        user.getZ(),
        SoundEvents.SNOWBALL_THROW,
        SoundSource.NEUTRAL,
        0.5F,
        0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

    var entity = new FlyingItemEntity((ItemStack) (Object) this, user, world);
    entity.setPosRaw(user.getX(), user.getEyeY() - 0.1F, user.getZ());
    entity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.5F, 1.0F);
    world.addFreshEntity(entity);

    user.getCooldowns()
        .addCooldown(
            getItem(),
            ItemBehavior.getCooldown((ServerLevel) world, user, entity, (ItemStack) (Object) this));
    user.awardStat(Stats.ITEM_USED.get(getItem()));

    if (!user.getAbilities().instabuild) {
      this.shrink(1);
    }
    return true;
  }
}
