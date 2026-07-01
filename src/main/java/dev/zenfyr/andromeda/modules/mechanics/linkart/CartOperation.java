package dev.zenfyr.andromeda.modules.mechanics.linkart;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;

public record CartOperation(Type type, AbstractMinecart minecart) {

  public enum Type {
    LINKING {
      @Override
      public InteractionResult perform(
          AbstractMinecart minecart, CartOperation operation, ItemStack stack) {
        if (((LinkableMinecart) minecart).linkart$getFollower() == operation.minecart())
          return InteractionResult.FAIL; // Linking a parent cart to its follower.
        if (((LinkableMinecart) minecart).linkart$getFollowing() != null)
          return InteractionResult.FAIL; // Linking to an already linked cart.

        var cfg = minecart.level().am$get(Linkart.CONFIG);
        if (Math.abs(minecart.distanceTo(operation.minecart()) - 1) > cfg.pathfindingDistance)
          return InteractionResult
              .FAIL; // Linking beyond pathfindingDistance, will just break on first tick.

        // Leading minecarts must never be linked to a follower. This creates an immovable object or
        // an Ouroboros, if you will.
        if (((LinkableMinecart) minecart).linkart$getFollower() != null
            && ((LinkableMinecart) minecart).linkart$getFollowing() == null) {
          var temp = minecart;
          while (temp != null) {
            if (temp == operation.minecart()) return InteractionResult.FAIL;
            temp = ((LinkableMinecart) temp).linkart$getFollower();
          }
        }

        LinkartMain.linkTo(minecart, operation.minecart(), stack);
        return InteractionResult.SUCCESS;
      }
    },
    UNLINKING {
      @Override
      public InteractionResult perform(
          AbstractMinecart minecart, CartOperation operation, ItemStack stack) {
        if (((LinkableMinecart) operation.minecart()).linkart$getFollower() != minecart)
          return InteractionResult.FAIL;

        LinkartMain.unlinkFromParent(minecart);
        return InteractionResult.SUCCESS;
      }
    };

    public abstract InteractionResult perform(
        AbstractMinecart minecart, CartOperation operation, ItemStack stack);
  }
}
