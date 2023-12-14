package me.melontini.andromeda.modules.misc.minor_inconvenience.mixin;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.misc.minor_inconvenience.Agony;
import me.melontini.andromeda.modules.misc.minor_inconvenience.MinorInconvenience;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin extends LivingEntity {
    @Unique
    private static final MinorInconvenience am$mininc = ModuleManager.quick(MinorInconvenience.class);
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/entity/LivingEntity.damage (Lnet/minecraft/entity/damage/DamageSource;F)Z", shift = At.Shift.BEFORE), method = "damage", cancellable = true)
    private void andromeda$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (am$mininc.enabled() && !world.isClient && source != Agony.AGONY) {
            super.damage(Agony.AGONY, Float.MAX_VALUE);
            this.world.createExplosion(null, Agony.AGONY, null, this.getBlockX() + 0.5, this.getBlockY() + 0.5, this.getBlockZ() + 0.5, 5.0F, true, Explosion.DestructionType.DESTROY);
            cir.setReturnValue(false);
        }
    }
}
