package dev.zenfyr.andromeda.modules.entities.villagers_follow_emeralds.mixin;

import dev.zenfyr.andromeda.modules.entities.villagers_follow_emeralds.VillagerTemptGoal;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
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
              target =
                  "Lnet/minecraft/world/entity/npc/Villager;setVillagerData(Lnet/minecraft/world/entity/npc/VillagerData;)V",
              shift = At.Shift.AFTER),
      method =
          "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Holder;)V")
  private void andromeda$init(
      EntityType<? extends Villager> entityType,
      Level level,
      Holder<VillagerType> holder,
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
}
