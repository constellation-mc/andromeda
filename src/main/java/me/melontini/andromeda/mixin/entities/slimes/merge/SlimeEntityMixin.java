package me.melontini.andromeda.mixin.entities.slimes.merge;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.minecraft.data.NbtUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeEntity.class)
@Feature("slimes.merge")
abstract class SlimeEntityMixin extends MobEntity {

    @Unique
    private int andromeda$mergeCD = MathStuff.nextInt(700, 2000);
    @Shadow public abstract int getSize();

    @Shadow public abstract void setSize(int size, boolean heal);

    protected SlimeEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "initGoals")
    private void andromeda$newGoal(CallbackInfo ci) {
        if (!Config.get().slimes.merge) return;

        this.targetSelector.add(2, new ActiveTargetGoal<>((SlimeEntity) (Object) this, SlimeEntity.class, 5, true, false, livingEntity -> {
            if (!Config.get().slimes.merge) return false;
            if (this.andromeda$mergeCD > 0) return false;
            float d = livingEntity.distanceTo((SlimeEntity) (Object) this);
            return d <= 6 && (getSize() <= Config.get().slimes.maxMerge && ((SlimeEntity) livingEntity).getSize() < getSize());
        }));
    }

    @Inject(at = @At("TAIL"), method = "pushAwayFrom")
    private void andromeda$push(Entity entity, CallbackInfo ci) {
        if (!Config.get().slimes.merge) return;

        if (getTarget() instanceof SlimeEntity slime && slime == entity && this.andromeda$mergeCD == 0) {
            int size = (int) Math.round(slime.getSize() * 0.75 + getSize() * 0.75);

            slime.discard();
            this.setSize(size, true);
            this.andromeda$mergeCD = MathStuff.nextInt(700, 2000);
            AndromedaLog.devInfo("Slime {} and {} merged, cooldown is {}!", this.toString(), slime.toString(), this.andromeda$mergeCD);
        }
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void andromeda$tick(CallbackInfo ci) {
        if (Config.get().slimes.merge) if (this.andromeda$mergeCD > 0) --this.andromeda$mergeCD;
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
    private void andromeda$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        if (Config.get().slimes.merge) nbt.putInt("AM-MergeCD", Math.max(this.andromeda$mergeCD, 0));
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    private void andromeda$readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (Config.get().slimes.merge) this.andromeda$mergeCD = NbtUtil.getInt(nbt, "AM-MergeCD", MathStuff.nextInt(700, 2000));
    }
}
