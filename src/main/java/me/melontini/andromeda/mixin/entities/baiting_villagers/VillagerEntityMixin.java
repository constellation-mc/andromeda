package me.melontini.andromeda.mixin.entities.baiting_villagers;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.entity.ai.goal.VillagerTemptGoal;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
@MixinRelatedConfigOption("villagersFollowEmeraldBlocks")
public abstract class VillagerEntityMixin extends MerchantEntity {

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;setVillagerData(Lnet/minecraft/village/VillagerData;)V", shift = At.Shift.AFTER), method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;Lnet/minecraft/village/VillagerType;)V")
    private void andromeda$init(EntityType<? extends VillagerEntity> entityType, World world, VillagerType type, CallbackInfo ci) {
        if (Config.get().villagersFollowEmeraldBlocks)
            this.goalSelector.add(6, new VillagerTemptGoal((VillagerEntity) (Object) this, 0.5, Ingredient.ofItems(Items.EMERALD_BLOCK), false));
    }
}
