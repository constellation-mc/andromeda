package me.melontini.andromeda.modules.misc.minor_inconvenience.mixin;

import me.melontini.andromeda.modules.misc.minor_inconvenience.MinorInconvenience;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static me.melontini.andromeda.modules.misc.minor_inconvenience.Main.AGONY;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/entity/LivingEntity.damage (Lnet/minecraft/entity/damage/DamageSource;F)Z", shift = At.Shift.BEFORE), method = "damage", cancellable = true)
    private void andromeda$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient && !source.isOf(AGONY) && world.am$get(MinorInconvenience.class).enabled) {
            Optional<RegistryEntry.Reference<DamageType>> type = this.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).getEntry(AGONY);
            DamageSource damagesource = new DamageSource(type.orElseThrow(), this);
            super.damage(damagesource, Float.MAX_VALUE);
            this.getWorld().createExplosion(null, damagesource, null, this.getBlockX() + 0.5, this.getBlockY() + 0.5, this.getBlockZ() + 0.5, 5.0F, true, World.ExplosionSourceType.MOB);
            cir.setReturnValue(false);
        }
    }
}
