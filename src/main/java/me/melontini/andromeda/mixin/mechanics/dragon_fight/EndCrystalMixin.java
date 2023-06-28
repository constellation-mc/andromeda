package me.melontini.andromeda.mixin.mechanics.dragon_fight;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.WorldUtil;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.util.MathStuff;
import me.melontini.dark_matter.util.Utilities;
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
@MixinRelatedConfigOption({"dragonFight.fightTweaks", "dragonFight.respawnCrystals"})
public abstract class EndCrystalMixin extends Entity {
    public EndCrystalMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean shouldShowBottom();

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/EndCrystalEntity;remove(Lnet/minecraft/entity/Entity$RemovalReason;)V", shift = At.Shift.BEFORE), method = "damage")
    private void andromeda$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (Andromeda.CONFIG.dragonFight.fightTweaks && Andromeda.CONFIG.dragonFight.respawnCrystals)
            if (world.getRegistryKey() == World.END && !((ServerWorld) world).getAliveEnderDragons().isEmpty() && shouldShowBottom()) {
                if (this.getPos().getY() > 71)
                    WorldUtil.getEnderDragonManager((ServerWorld) world).queueRespawn(new MutableInt(MathStuff.nextInt(Utilities.RANDOM, 1900, 3500)), this.getPos());
            }
    }
}
