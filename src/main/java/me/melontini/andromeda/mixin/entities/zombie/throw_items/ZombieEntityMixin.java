package me.melontini.andromeda.mixin.entities.zombie.throw_items;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.entity.FlyingItemEntity;
import me.melontini.andromeda.entity.ai.goal.ItemThrowerMob;
import me.melontini.andromeda.entity.ai.goal.ThrowableItemAttackGoal;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
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
@MixinRelatedConfigOption({"newThrowableItems.enable", "newThrowableItems.canZombiesThrowItems"})
public abstract class ZombieEntityMixin extends HostileEntity implements ItemThrowerMob<ZombieEntity> {

    @Unique
    private int andromeda$cooldown = 0;

    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "initCustomGoals")
    private void initCustomGoals(CallbackInfo ci) {
        if (Andromeda.CONFIG.newThrowableItems.enable &&
                Andromeda.CONFIG.newThrowableItems.canZombiesThrowItems)
            this.goalSelector.add(1, new ThrowableItemAttackGoal<>(this, 1.0f, Andromeda.CONFIG.newThrowableItems.zombieThrowInterval, 4, 16));
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void tick(CallbackInfo ci) {
        if (this.andromeda$cooldown > 0) this.andromeda$cooldown--;
    }

    @Override
    public void am$throwItem(LivingEntity target, float pullProgress) {
        if (!Andromeda.CONFIG.newThrowableItems.enable ||
                !Andromeda.CONFIG.newThrowableItems.canZombiesThrowItems) return;

        world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        var entity = andromeda$getFlyingItemEntity(target);
        world.spawnEntity(entity);
        this.getMainHandStack().decrement(1);
        if (Utilities.RANDOM.nextBoolean())
            this.andromeda$cooldown += MathStuff.nextInt(Utilities.RANDOM, (int) (this.distanceTo(target) * 28) / 2, (int) (this.distanceTo(target) * 28));
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

    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/HostileEntity;canPickupItem(Lnet/minecraft/item/ItemStack;)Z"), method = "canPickupItem")
    public boolean andromeda$canPickupItem(boolean original, ItemStack stack) {
        if (Andromeda.CONFIG.newThrowableItems.enable &&
                Andromeda.CONFIG.newThrowableItems.canZombiesThrowItems &&
                Andromeda.CONFIG.newThrowableItems.preventUselessItems)
            return original && (stack.getItem() instanceof ToolItem || stack.getItem() instanceof ArmorItem || ItemBehaviorManager.hasBehaviors(stack.getItem()));
        else
            return original;
    }

    @Inject(at = @At("HEAD"), method = "writeCustomDataToNbt")
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
       if (this.andromeda$cooldown > 0) nbt.putInt("AM-Throw-Cooldown", this.andromeda$cooldown);
    }

    @Inject(at = @At("HEAD"), method = "readCustomDataFromNbt")
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("AM-Throw-Cooldown")) this.andromeda$cooldown = nbt.getInt("AM-Throw-Cooldown");
    }

}
