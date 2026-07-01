package dev.zenfyr.andromeda.modules.mechanics.linkart.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.mechanics.linkart.LinkableMinecart;
import dev.zenfyr.andromeda.modules.mechanics.linkart.LinkartMain;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

  @Inject(at = @At("HEAD"), method = "remove")
  void linkart$removeLink(
      CallbackInfo callbackInformation, @Local(argsOnly = true) Entity.RemovalReason reason) {
    if ((Entity) (Object) this instanceof AbstractMinecart minecart
        && !minecart.level().isClientSide()
        && reason.shouldDestroy()) {
      LinkartMain.unlinkFromParent(minecart);
      LinkartMain.unlinkFromParent(((LinkableMinecart) minecart).linkart$getFollower());
    }
  }

  @Inject(at = @At("HEAD"), method = "collide", cancellable = true)
  void linkart$onRecalculateVelocity(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
    if ((Object) this instanceof AbstractMinecart minecart) {
      List<Entity> collisions = minecart
          .level()
          .getEntities((Entity) (Object) this, minecart.getBoundingBox().expandTowards(movement));

      for (Entity entity : collisions) {
        if (!LinkartMain.shouldCollide(minecart, entity)
            && minecart.level().getBlockState(minecart.blockPosition()).getBlock()
                instanceof BaseRailBlock) {
          cir.setReturnValue(movement);
          return;
        }
      }
    }
  }
}
