package me.melontini.andromeda.modules.entities.slimes.mixin.merge;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.slimes.Slimes;
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
abstract class SlimeEntityMixin extends MobEntity {
    @Unique
    private static final Slimes am$slimes = ModuleManager.quick(Slimes.class);

    @Shadow public abstract int getSize();
    @Shadow public abstract void setSize(int size, boolean heal);

    @Unique
    private int andromeda$mergeCD = MathStuff.nextInt(700, 2000);

    protected SlimeEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "initGoals")
    private void andromeda$newGoal(CallbackInfo ci) {
        if (!am$slimes.config().merge) return;

        this.targetSelector.add(2, new ActiveTargetGoal<>((SlimeEntity) (Object) this, SlimeEntity.class, 5, true, false, livingEntity -> {
            if (!am$slimes.config().merge) return false;
            if (this.andromeda$mergeCD > 0) return false;
            float d = livingEntity.distanceTo((SlimeEntity) (Object) this);
            return d <= 6 && (getSize() <= am$slimes.config().maxMerge && ((SlimeEntity) livingEntity).getSize() < getSize());
        }));
    }

    @Inject(at = @At("TAIL"), method = "pushAwayFrom")
    private void andromeda$push(Entity entity, CallbackInfo ci) {
        if (!am$slimes.config().merge) return;

        if (getTarget() instanceof SlimeEntity slime && slime == entity && this.andromeda$mergeCD == 0) {
            int size = (int) Math.round(slime.getSize() * 0.75 + getSize() * 0.75);

            slime.discard();
            this.setSize(size, true);
            this.andromeda$mergeCD = MathStuff.nextInt(700, 2000);
        }
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void andromeda$tick(CallbackInfo ci) {
        if (am$slimes.config().merge) if (this.andromeda$mergeCD > 0) --this.andromeda$mergeCD;
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
    private void andromeda$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        if (am$slimes.config().merge) nbt.putInt("AM-MergeCD", Math.max(this.andromeda$mergeCD, 0));
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    private void andromeda$readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (am$slimes.config().merge) this.andromeda$mergeCD = NbtUtil.getInt(nbt, "AM-MergeCD", MathStuff.nextInt(700, 2000));
    }
}
