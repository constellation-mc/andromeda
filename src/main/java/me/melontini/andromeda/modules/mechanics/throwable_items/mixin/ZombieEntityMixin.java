package me.melontini.andromeda.modules.mechanics.throwable_items.mixin;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import me.melontini.andromeda.modules.mechanics.throwable_items.ItemThrowerMob;
import me.melontini.andromeda.modules.mechanics.throwable_items.ThrowableItemAttackGoal;
import me.melontini.andromeda.modules.mechanics.throwable_items.ThrowableItems;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
import me.melontini.dark_matter.api.base.util.MathStuff;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieEntity.class)
abstract class ZombieEntityMixin extends HostileEntity implements ItemThrowerMob<ZombieEntity> {
    @Unique
    private static final ThrowableItems am$thritm = ModuleManager.quick(ThrowableItems.class);

    @Unique
    private int andromeda$cooldown = 0;

    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "initCustomGoals")
    private void andromeda$initCustomGoals(CallbackInfo ci) {
        if (am$thritm.config().enabled && am$thritm.config().canZombiesThrowItems)
            this.goalSelector.add(1, new ThrowableItemAttackGoal<>(this, 1.0f, am$thritm.config().zombieThrowInterval, 4, 16));
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void andromeda$tick(CallbackInfo ci) {
        if (this.andromeda$cooldown > 0) this.andromeda$cooldown--;
    }

    @Override
    public void am$throwItem(LivingEntity target, float pullProgress) {
        if (!am$thritm.config().enabled || !am$thritm.config().canZombiesThrowItems) return;

        world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        var entity = andromeda$getFlyingItemEntity(target);
        world.spawnEntity(entity);
        if (MathStuff.threadRandom().nextBoolean())
            this.andromeda$cooldown += Math.max(MathStuff.nextInt((int) (this.distanceTo(target) * 28) / 2, (int) (this.distanceTo(target) * 28)), ItemBehaviorManager.getCooldown(this.getMainHandStack().getItem()));
        this.getMainHandStack().decrement(1);
    }

    @Unique
    @NotNull
    private FlyingItemEntity andromeda$getFlyingItemEntity(LivingEntity target) {
        var entity = new FlyingItemEntity(this.getMainHandStack(), this, world);
        entity.setPos(this.getX(), this.getEyeY() - 0.1F, this.getZ());
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - entity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        entity.setVelocity(d, e + g * 0.2F, f, 0.9F, 9.0F);
        return entity;
    }

    @Override
    public int am$cooldown() {
        return this.andromeda$cooldown;
    }

    @Inject(at = @At("HEAD"), method = "writeCustomDataToNbt")
    private void andromeda$writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
       if (this.andromeda$cooldown > 0) nbt.putInt("AM-Throw-Cooldown", this.andromeda$cooldown);
    }

    @Inject(at = @At("HEAD"), method = "readCustomDataFromNbt")
    private void andromeda$readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("AM-Throw-Cooldown")) this.andromeda$cooldown = nbt.getInt("AM-Throw-Cooldown");
    }
}
