package me.melontini.andromeda.modules.entities.vehicle_unentrapment.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.modules.entities.vehicle_unentrapment.Tags;
import me.melontini.andromeda.modules.entities.vehicle_unentrapment.VehicleUnentrapment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract void stopRiding();

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "net/minecraft/entity/LivingEntity.isSleeping()Z"))
    private void andromeda$sos(CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) DamageSource source) {
        if (!world.isClient() && world.am$get(VehicleUnentrapment.class).enabled) {
            if (source.getAttacker() == null || (Object) this instanceof PlayerEntity) return;
            if (!this.getType().isIn(Tags.ESCAPE_VEHICLES_ON_HIT)) return;

            Entity entity = this.getVehicle();
            if (entity == null || !entity.getType().isIn(Tags.ESCAPABLE_VEHICLES)) return;
            this.stopRiding();
        }
    }
}
