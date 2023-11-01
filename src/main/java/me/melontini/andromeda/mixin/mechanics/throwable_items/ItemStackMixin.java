package me.melontini.andromeda.mixin.mechanics.throwable_items;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.entity.FlyingItemEntity;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
@Feature("throwableItems.enable")
abstract class ItemStackMixin {

    @Shadow public abstract Item getItem();
    @Shadow public abstract void decrement(int amount);

    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    private void andromeda$throwableBehaviour(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (ItemBehaviorManager.hasBehaviors(getItem()) && ItemBehaviorManager.overridesVanilla(getItem())) {
            if (andromeda$runBehaviors(world, user)) {
                cir.setReturnValue(TypedActionResult.success((ItemStack) (Object) this));
            }
        }
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "use")
    private TypedActionResult<ItemStack> andromeda$throwableBehaviour(TypedActionResult<ItemStack> original, World world, PlayerEntity user, Hand hand) {
        if (original.getResult() == ActionResult.PASS && ItemBehaviorManager.hasBehaviors(getItem()) && !ItemBehaviorManager.overridesVanilla(getItem())) {
            if (andromeda$runBehaviors(world, user)) {
                return TypedActionResult.success((ItemStack) (Object) this);
            }
        }
        return original;
    }

    @Unique
    private boolean andromeda$runBehaviors(World world, PlayerEntity user) {
        if (!Config.get().throwableItems.enable) return false;

        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));
        if (!world.isClient) {
            var entity = new FlyingItemEntity((ItemStack) (Object) this, user, world);
            entity.setPos(user.getX(), user.getEyeY() - 0.1F, user.getZ());
            entity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
            world.spawnEntity(entity);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(getItem()));
        if (!user.getAbilities().creativeMode) {
            this.decrement(1);
        }

        user.getItemCooldownManager().set(getItem(), ItemBehaviorManager.getCooldown(getItem()));
        return true;
    }
}
