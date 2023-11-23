package me.melontini.andromeda.mixin.entities.zombie.all_pick_up;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.zombie.all_pick_up.Pickup;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieEntity.class)
abstract class ZombieEntityMixin extends HostileEntity {
    @Unique
    private static final Pickup am$azcbi = ModuleManager.quick(Pickup.class);

    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/ZombieEntity;setCanPickUpLoot(Z)V", shift = At.Shift.AFTER), method = "initialize")
    private void andromeda$initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityNbt, CallbackInfoReturnable<EntityData> cir) {
        if (am$azcbi.config().enabled) this.setCanPickUpLoot(true);
    }
}
