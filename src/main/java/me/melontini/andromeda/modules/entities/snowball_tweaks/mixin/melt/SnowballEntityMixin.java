package me.melontini.andromeda.modules.entities.snowball_tweaks.mixin.melt;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.snowball_tweaks.Snowballs;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEntity.class)
abstract class SnowballEntityMixin extends ProjectileEntity {
    @Unique
    private static final Snowballs am$snow = ModuleManager.quick(Snowballs.class);

    public SnowballEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "tick()V")
    public void andromeda$melt(CallbackInfo ci) {
        if ((Object) this instanceof SnowballEntity) {
            if (world.isClient() || !this.isOnFire()) return;

            Snowballs.Config config = world.am$get(am$snow);
            if (!config.enabled || !config.melt) return;

            ((ServerWorld) world).spawnParticles(ParticleTypes.FALLING_WATER, this.getX(), this.getY(), this.getZ(), 10, 0.5, 0.5, 0.5, 0.4);
            this.discard();
        }
    }
}
