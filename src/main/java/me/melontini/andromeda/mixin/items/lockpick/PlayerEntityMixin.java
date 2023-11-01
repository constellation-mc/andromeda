package me.melontini.andromeda.mixin.items.lockpick;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.melontini.andromeda.items.LockpickItem;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
@Feature("lockpick.enable")
abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"), method = "interact")
    private ActionResult andromeda$stopInteract(Entity entity, PlayerEntity player, Hand hand, Operation<ActionResult> original) {
        if (getStackInHand(Hand.MAIN_HAND).getItem() instanceof LockpickItem || getStackInHand(Hand.OFF_HAND).getItem() instanceof LockpickItem) {
            return ActionResult.PASS;
        }
        return original.call(entity, player, hand);
    }
}
