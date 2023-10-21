package me.melontini.andromeda.mixin.entities.snowball_tweaks.melt;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.base.util.mixin.annotations.ConstructDummy;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowballEntity.class)
@Feature("snowballs.melt")
abstract class SnowballEntityMixin extends ThrownItemEntity {

    public SnowballEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @ConstructDummy(owner = "net.minecraft.class_1297", name = "method_5773", desc = "()V")
    @Inject(at = @At("HEAD"), method = "tick()V")
    public void andromeda$melt(CallbackInfo ci) {
        if (!Config.get().snowballs.melt || !this.isOnFire()) return;

        if (!world.isClient()) ((ServerWorld) world).spawnParticles(ParticleTypes.FALLING_WATER, this.getX(), this.getY(), this.getZ(), 10, 0.5, 0.5, 0.5, 0.4);
        this.discard();
    }
}
