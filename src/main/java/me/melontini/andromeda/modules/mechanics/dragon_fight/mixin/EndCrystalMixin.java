package me.melontini.andromeda.modules.mechanics.dragon_fight.mixin;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.mechanics.dragon_fight.DragonFight;
import me.melontini.andromeda.modules.mechanics.dragon_fight.EnderDragonManager;
import me.melontini.dark_matter.api.base.util.MathStuff;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystalEntity.class)
abstract class EndCrystalMixin extends Entity {

    @Shadow public abstract boolean shouldShowBottom();

    public EndCrystalMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/EndCrystalEntity;remove(Lnet/minecraft/entity/Entity$RemovalReason;)V", shift = At.Shift.BEFORE), method = "damage")
    private void andromeda$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!ModuleManager.quick(DragonFight.class).config().respawnCrystals) return;

        if (world.getRegistryKey() == World.END && !((ServerWorld) world).getAliveEnderDragons().isEmpty() && shouldShowBottom()) {
            if (this.getPos().getY() <= 71) return;
            ((ServerWorld)world).getAttachedOrCreate(EnderDragonManager.ATTACHMENT.get()).queueRespawn(new MutableInt(MathStuff.nextInt(1900, 3500)), this.getPos());
        }
    }
}
