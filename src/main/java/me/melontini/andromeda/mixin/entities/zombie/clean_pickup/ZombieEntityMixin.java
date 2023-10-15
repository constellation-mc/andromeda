package me.melontini.andromeda.mixin.entities.zombie.clean_pickup;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.registries.TagRegistry;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ZombieEntity.class)
@MixinRelatedConfigOption("zombiesPreventUselessItems")
abstract class ZombieEntityMixin extends HostileEntity {

    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/HostileEntity;canPickupItem(Lnet/minecraft/item/ItemStack;)Z"), method = "canPickupItem")
    public boolean andromeda$canPickupItem(boolean original, ItemStack stack) {
        if (Config.get().zombiesPreventUselessItems)
            return original && (stack.getItem() instanceof ToolItem || stack.getItem() instanceof ArmorItem ||
                    stack.isIn(TagRegistry.get().ZOMBIES_PICKUP) ||
                    (Config.get().throwableItems.canZombiesThrowItems && ItemBehaviorManager.hasBehaviors(stack.getItem())));
        else
            return original;
    }
}
