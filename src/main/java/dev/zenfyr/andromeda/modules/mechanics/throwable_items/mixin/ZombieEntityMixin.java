package dev.zenfyr.andromeda.modules.mechanics.throwable_items.mixin;

import dev.zenfyr.andromeda.modules.mechanics.throwable_items.*;
import dev.zenfyr.pulsar.util.MathUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
abstract class ZombieEntityMixin extends Monster implements ItemThrowerMob<Zombie> {

  @Unique private int andromeda$cooldown = 0;

  protected ZombieEntityMixin(EntityType<? extends Monster> entityType, Level world) {
    super(entityType, world);
  }

  @Inject(at = @At("HEAD"), method = "addBehaviourGoals")
  private void andromeda$initCustomGoals(CallbackInfo ci) {
    if (level.am$get(ThrowableItems.CONFIG).canZombiesThrowItems)
      this.goalSelector.addGoal(1, new ThrowableItemAttackGoal<>(this, 1.0f, 4, 16));
  }

  @Inject(at = @At("HEAD"), method = "tick")
  private void andromeda$tick(CallbackInfo ci) {
    if (this.andromeda$cooldown > 0) this.andromeda$cooldown--;
  }

  @Override
  public void am$throwItem(LivingEntity target, float pullProgress) {
    if (!level.am$get(ThrowableItems.CONFIG).canZombiesThrowItems) return;

    level.playSound(
        null,
        this.getX(),
        this.getY(),
        this.getZ(),
        SoundEvents.SNOWBALL_THROW,
        SoundSource.NEUTRAL,
        0.5F,
        0.4F / (level.random.nextFloat() * 0.4F + 0.8F));

    var entity = andromeda$getFlyingItemEntity(target);
    level.addFreshEntity(entity);
    if (MathUtil.threadRandom().nextBoolean())
      this.andromeda$cooldown += Math.max(
          MathUtil.nextInt(
              (int) (this.distanceTo(target) * 28) / 2, (int) (this.distanceTo(target) * 28)),
          ItemBehavior.getCooldown((ServerLevel) level, this, entity, this.getMainHandItem()));
    this.getMainHandItem().shrink(1);
  }

  @Unique @NotNull private FlyingItemEntity andromeda$getFlyingItemEntity(LivingEntity target) {
    var entity = new FlyingItemEntity(this.getMainHandItem(), this, level);
    entity.setPosRaw(this.getX(), this.getEyeY() - 0.1F, this.getZ());
    double d = target.getX() - this.getX();
    double e = target.getY(0.3333333333333333) - entity.getY();
    double f = target.getZ() - this.getZ();
    double g = Math.sqrt(d * d + f * f);
    entity.shoot(d, e + g * 0.2F, f, 0.9F, 9.0F);
    return entity;
  }

  @Override
  public int am$cooldown() {
    return this.andromeda$cooldown;
  }

  @Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
  private void andromeda$writeCustomDataToNbt(CompoundTag nbt, CallbackInfo ci) {
    if (this.andromeda$cooldown > 0) nbt.putInt("AM-Throw-Cooldown", this.andromeda$cooldown);
  }

  @Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
  private void andromeda$readCustomDataFromNbt(CompoundTag nbt, CallbackInfo ci) {
    if (nbt.contains("AM-Throw-Cooldown"))
      this.andromeda$cooldown = nbt.getInt("AM-Throw-Cooldown");
  }
}
