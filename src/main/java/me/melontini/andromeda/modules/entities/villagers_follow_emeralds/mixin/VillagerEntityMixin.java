package me.melontini.andromeda.modules.entities.villagers_follow_emeralds.mixin;

import me.melontini.andromeda.modules.entities.villagers_follow_emeralds.VillagerTemptGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.entity.npc.VillagerType;
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
              "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/npc/VillagerType;)V")
  private void andromeda$init(
          EntityType<? extends Villager> entityType,
          Level world,
          VillagerType type,
          CallbackInfo ci) {
    this.goalSelector.addGoal(
        6,
        new VillagerTemptGoal(
            (Villager) (Object) this,
            0.5,
            Ingredient.of(VillagerTemptGoal.TEMPTING),
            false));
  }
}
