package me.melontini.andromeda.modules.entities.slimes.mixin.slowness;

import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.entities.slimes.Slimes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeEntity.class)
abstract class SlimeEntityMixin extends MobEntity {

    @Shadow public abstract int getSize();

    @Shadow protected abstract ParticleEffect getParticles();

    protected SlimeEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/SlimeEntity;getSize()I", shift = At.Shift.BEFORE), method = "damage")
    private void andromeda$onPlayerCollision(LivingEntity target, CallbackInfo ci) {
        var config = this.world.am$get(Slimes.CONFIG);
        if (!config.available.asBoolean(ConstantLootContextAccessor.get(this))) return;
        if (!config.slowness.asBoolean(LootContextUtil.entity(world, target.getPos(), target, null, this))) return;

        StatusEffectInstance effectInstance = new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * this.getSize(), 1, true, false, false);
        target.addStatusEffect(effectInstance);
        if (world.getTime() % 3 == 0) ((ServerWorld) world).spawnParticles(getParticles(), target.getX(), target.getY(), target.getZ(), 5, 0.2, 0.7, 0.2, 0);
    }
}
