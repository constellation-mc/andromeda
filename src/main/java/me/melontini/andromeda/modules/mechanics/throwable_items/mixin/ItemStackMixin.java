package me.melontini.andromeda.modules.mechanics.throwable_items.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
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
abstract class ItemStackMixin {

    @Shadow public abstract Item getItem();
    @Shadow public abstract void decrement(int amount);

    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    private void andromeda$throwableBehavior(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (world.isClient()) return;

        var manager = ItemBehaviorManager.get(world.getServer());
        if (manager.hasBehaviors(getItem()) && manager.overridesVanilla(getItem())) {
            if (andromeda$runBehaviors(world, manager, user)) {
                cir.setReturnValue(TypedActionResult.success((ItemStack) (Object) this));
            }
        }
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "use")
    private TypedActionResult<ItemStack> andromeda$throwableBehavior(TypedActionResult<ItemStack> original, World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return original;

        var manager = ItemBehaviorManager.get(world.getServer());
        if (original.getResult() == ActionResult.PASS && manager.hasBehaviors(getItem()) && !manager.overridesVanilla(getItem())) {
            if (andromeda$runBehaviors(world, manager, user)) {
                return TypedActionResult.success((ItemStack) (Object) this);
            }
        }
        return original;
    }

    @Unique
    private boolean andromeda$runBehaviors(World world, ItemBehaviorManager manager, PlayerEntity user) {
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));
        if (!world.isClient) {
            var entity = new FlyingItemEntity((ItemStack) (Object) this, user, world);
            entity.setPos(user.getX(), user.getEyeY() - 0.1F, user.getZ());
            entity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
            world.spawnEntity(entity);
        }

        user.getItemCooldownManager().set(getItem(), manager.getCooldown(getItem()));
        user.incrementStat(Stats.USED.getOrCreateStat(getItem()));

        if (!user.getAbilities().creativeMode) {
            this.decrement(1);
        }
        return true;
    }
}
