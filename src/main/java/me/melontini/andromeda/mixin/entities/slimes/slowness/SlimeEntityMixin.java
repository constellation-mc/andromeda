package me.melontini.andromeda.mixin.entities.slimes.slowness;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeEntity.class)
@MixinRelatedConfigOption("slimes.slowness")
abstract class SlimeEntityMixin extends MobEntity {

    @Shadow
    public abstract int getSize();

    protected SlimeEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/SlimeEntity;getSize()I", shift = At.Shift.BEFORE), method = "damage")
    private void andromeda$onPlayerCollision(LivingEntity target, CallbackInfo ci) {
        if (!Config.get().slimes.slowness) return;

        StatusEffectInstance effectInstance = new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * this.getSize(), 1, true, false, false);
        target.addStatusEffect(effectInstance);
    }
}
