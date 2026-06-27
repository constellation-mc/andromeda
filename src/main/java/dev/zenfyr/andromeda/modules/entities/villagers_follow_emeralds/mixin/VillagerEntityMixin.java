package dev.zenfyr.andromeda.modules.entities.villagers_follow_emeralds.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.zenfyr.andromeda.modules.entities.villagers_follow_emeralds.VillagerTemptGoal;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
abstract class VillagerEntityMixin extends AbstractVillager {

  public VillagerEntityMixin(EntityType<? extends AbstractVillager> entityType, Level world) {
    super(entityType, world);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/entity/npc/villager/Villager;setCanPickUpLoot(Z)V",
              shift = At.Shift.AFTER),
      method = "<init>")
  private void andromeda$init(EntityType<?> entityType, Level level, CallbackInfo ci) {
    level
        .registryAccess()
        .lookup(Registries.ITEM)
        .flatMap(items -> items.get(VillagerTemptGoal.TEMPTING))
        .ifPresent(itemHolderSet -> this.goalSelector.addGoal(
            6,
            new VillagerTemptGoal(
                (Villager) (Object) this, 0.5, Ingredient.of(itemHolderSet), false)));
  }

  @ModifyReturnValue(at = @At("RETURN"), method = "createAttributes")
  private static AttributeSupplier.Builder andromeda$addTemptRange(
      AttributeSupplier.Builder original) {
    return original.add(Attributes.TEMPT_RANGE, 10);
  }
}
