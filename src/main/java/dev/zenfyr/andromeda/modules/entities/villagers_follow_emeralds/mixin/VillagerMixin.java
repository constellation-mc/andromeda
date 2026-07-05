package dev.zenfyr.andromeda.modules.entities.villagers_follow_emeralds.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.zenfyr.andromeda.modules.entities.villagers_follow_emeralds.VillagerTemptGoal;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
abstract class VillagerMixin extends AbstractVillager {

  public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
    super(entityType, level);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/npc/villager/Villager;setVillagerData(Lnet/minecraft/world/entity/npc/villager/VillagerData;)V",
              shift = At.Shift.AFTER),
      method =
          "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Holder;)V")
  private void andromeda$init(
      EntityType<? extends Villager> entityType,
      Level level,
      Holder<VillagerType> type,
      CallbackInfo ci) {
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
