package me.melontini.andromeda.mixin.entities.zombie.clean_pickup;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.zombie.clean_pickup.Pickup;
import me.melontini.andromeda.modules.entities.zombie.clean_pickup.PickupTag;
import me.melontini.andromeda.modules.mechanics.throwable_items.ThrowableItems;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ZombieEntity.class)
abstract class ZombieEntityMixin extends HostileEntity {
    @Unique
    private static final Pickup am$zpui = ModuleManager.quick(Pickup.class);

    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/HostileEntity;canPickupItem(Lnet/minecraft/item/ItemStack;)Z"), method = "canPickupItem")
    public boolean andromeda$canPickupItem(boolean original, ItemStack stack) {
        if (am$zpui.config().enabled)
            return original && (stack.getItem() instanceof ToolItem || stack.getItem() instanceof ArmorItem ||
                    stack.isIn(PickupTag.ZOMBIES_PICKUP) || ModuleManager.get().getModule(ThrowableItems.class)
                    .map(m -> handleThrowableItems(m, stack))
                    .orElse(false));
        else
            return original;
    }

    @Unique
    private boolean handleThrowableItems(ThrowableItems m, ItemStack stack) {
        return m.config().canZombiesThrowItems && ItemBehaviorManager.hasBehaviors(stack.getItem());
    }
}
