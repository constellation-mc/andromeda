package me.melontini.andromeda.mixin.blocks.bed.power;

import me.melontini.andromeda.Andromeda;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin extends Block {

    public BedBlockMixin(Settings settings) {
        super(settings);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;"), index = 6, method = "onUse")
    public float andromeda$explosionRedirect(float power) {
        return Andromeda.CONFIG.bedExplosionPower;
    }
}
